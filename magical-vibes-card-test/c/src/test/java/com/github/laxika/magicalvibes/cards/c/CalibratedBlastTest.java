package com.github.laxika.magicalvibes.cards.c;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.d.DarkmossBridge;
import com.github.laxika.magicalvibes.cards.m.MistvaultBridge;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@CardUsed({CalibratedBlast.class, DarkmossBridge.class, MistvaultBridge.class, OrnithopterOfParadise.class})
class CalibratedBlastTest extends BaseCardTest {

    @org.junit.jupiter.api.BeforeEach
    void keepPriorityForResponses() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.EnumSet.of(
                com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN,
                com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.EnumSet.of(
                com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN,
                com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE));
    }


    @Test
    void dealsDamageEqualToFirstNonlandManaValueAndRandomizesRevealedCardsToBottom() {
        DarkmossBridge firstLand = new DarkmossBridge();
        MistvaultBridge secondLand = new MistvaultBridge();
        OrnithopterOfParadise nonland = new OrnithopterOfParadise();
        harness.setLibrary(player1, List.of(firstLand, secondLand, nonland));
        harness.setHand(player1, List.of(new CalibratedBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
        resolveDamageTrigger(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstLand, secondLand, nonland);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void dealsDamageToAnyTargetCreature() {
        Permanent target = addCreatureReady(player2, new OrnithopterOfParadise());
        harness.setLibrary(player1, List.of(new OrnithopterOfParadise()));
        harness.setHand(player1, List.of(new CalibratedBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
        resolveDamageTrigger(target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target.getCard());
    }

    @Test
    void dealsNoDamageWhenTheLibraryHasNoNonlandCard() {
        DarkmossBridge firstLand = new DarkmossBridge();
        MistvaultBridge secondLand = new MistvaultBridge();
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        harness.setHand(player1, List.of(new CalibratedBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(firstLand, secondLand);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void flashbackDealsDamageAndExilesCalibratedBlast() {
        harness.setGraveyard(player1, List.of(new CalibratedBlast()));
        harness.setLibrary(player1, List.of(new OrnithopterOfParadise()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        resolveDamageTrigger(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertNotInGraveyard(player1, "Calibrated Blast");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Calibrated Blast"));
    }

    @Test
    void cannotTargetALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new DarkmossBridge());
        harness.setLibrary(player1, List.of(new OrnithopterOfParadise()));
        harness.setHand(player1, List.of(new CalibratedBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void emptyLibraryResolvesWithoutCreatingADamageTrigger() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CalibratedBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Calibrated Blast");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void bottomsOnlyRevealedCardsAndLeavesDamagePendingAfterSpellResolution() {
        DarkmossBridge revealedLand = new DarkmossBridge();
        OrnithopterOfParadise revealedNonland = new OrnithopterOfParadise();
        MistvaultBridge unrevealedLand = new MistvaultBridge();
        CalibratedBlast unrevealedNonland = new CalibratedBlast();
        harness.setLibrary(player1, List.of(
                revealedLand, revealedNonland, unrevealedLand, unrevealedNonland));
        harness.setHand(player1, List.of(new CalibratedBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Calibrated Blast");
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(unrevealedLand, unrevealedNonland);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 4))
                .containsExactlyInAnyOrder(revealedLand, revealedNonland);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    private void resolveDamageTrigger(UUID targetId) {
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }

}

package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GallantCitizen;
import com.github.laxika.magicalvibes.cards.p.PrisonBreak;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArachnePsionicWeaver.class, GallantCitizen.class, PrisonBreak.class, Shock.class})
class ArachnePsionicWeaverTest extends BaseCardTest {

    @Test
    void looksAtOpponentHandAndChoosesNoncreatureCardType() {
        harness.setHand(player2, List.of(new GallantCitizen(), new Shock()));
        harness.castFromHand(player1, new ArachnePsionicWeaver(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"));
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).doesNotContain(CardType.CREATURE.name());

        harness.handleListChoice(player1, CardType.INSTANT.name());

        Permanent arachne = findPermanent(player1, "Arachne, Psionic Weaver");
        assertThat(arachne.getChosenCardType()).isEqualTo(CardType.INSTANT);
    }

    @Test
    void chosenTypeTaxesMatchingSpellsForAllPlayers() {
        addReadyArachne(player1, CardType.INSTANT);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GallantCitizen());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        assertThat(gd.stack).hasSize(1);

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

    }

    @Test
    void spellsOfOtherTypesAreNotTaxed() {
        addReadyArachne(player1, CardType.INSTANT);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GallantCitizen()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void webSlingingReturnsTappedCreatureAsCostAndPaysOnlyWhiteMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GallantCitizen());
        creature.tap();
        harness.setHand(player1, List.of(new ArachnePsionicWeaver()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(creature.getId()));

        harness.assertInHand(player1, "Gallant Citizen");
        harness.assertNotOnBattlefield(player1, "Gallant Citizen");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Arachne, Psionic Weaver");
        harness.handleListChoice(player1, CardType.INSTANT.name());
        harness.assertOnBattlefield(player1, "Arachne, Psionic Weaver");
    }

    @Test
    void webSlingingCannotReturnUntappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GallantCitizen());
        harness.setHand(player1, List.of(new ArachnePsionicWeaver()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(
                player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Gallant Citizen");
        harness.assertInHand(player1, "Arachne, Psionic Weaver");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void webSlingingReturnsBorrowedCreatureToItsOwner() {
        GallantCitizen borrowed = new GallantCitizen();
        borrowed.setOwnerId(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, borrowed);
        gd.stolenCreatures.put(creature.getId(), player2.getId());
        creature.tap();
        harness.setHand(player1, List.of(new ArachnePsionicWeaver()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(creature.getId()));

        harness.assertInHand(player2, "Gallant Citizen");
        harness.assertNotInHand(player1, "Gallant Citizen");
        harness.assertNotOnBattlefield(player1, "Gallant Citizen");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void choosesTypeEvenWhenOpponentHandIsEmptyAndTaxesImmediately() {
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new ArachnePsionicWeaver(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Arachne, Psionic Weaver");
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, CardType.INSTANT.name());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reanimationStillLooksAtHandAndChoosesTypeBeforeEntering() {
        ArachnePsionicWeaver arachne = new ArachnePsionicWeaver();
        harness.setGraveyard(player1, List.of(arachne));
        harness.setHand(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new PrisonBreak()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, arachne.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"));
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).doesNotContain(CardType.CREATURE.name());
        harness.handleListChoice(player1, CardType.INSTANT.name());

        harness.assertOnBattlefield(player1, "Arachne, Psionic Weaver");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void addReadyArachne(Player player, CardType chosenType) {
        Permanent arachne = harness.addToBattlefieldAndReturn(player, new ArachnePsionicWeaver());
        arachne.setSummoningSick(false);
        arachne.setChosenCardType(chosenType);
    }
}

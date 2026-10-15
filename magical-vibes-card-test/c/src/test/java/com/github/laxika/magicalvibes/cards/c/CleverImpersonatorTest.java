package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.d.DebilitatingInjury;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MarduSkullhunter;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheDragonspeaker;
import com.github.laxika.magicalvibes.cards.s.SultaiSoothsayer;
import com.github.laxika.magicalvibes.cards.w.WoollyLoxodon;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CleverImpersonator.class, DarksteelRelic.class, Island.class,
        DebilitatingInjury.class, MarduSkullhunter.class, SarkhanTheDragonspeaker.class,
        SultaiSoothsayer.class, WoollyLoxodon.class})
class CleverImpersonatorTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a noncreature artifact")
    void copiesNoncreatureArtifact() {
        harness.addToBattlefield(player2, new DarksteelRelic());
        castCleverImpersonator();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        UUID relicId = harness.getPermanentId(player2, "Darksteel Relic");
        harness.handlePermanentChosen(player1, relicId);

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Clever Impersonator"))
                .findFirst().orElseThrow();
        assertThat(copy.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(copy.getCard().hasType(CardType.CREATURE)).isFalse();
    }

    @Test
    @DisplayName("Cannot copy a land")
    void cannotCopyLand() {
        harness.addToBattlefield(player2, new Island());
        castCleverImpersonator();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Clever Impersonator");
        harness.assertInGraveyard(player1, "Clever Impersonator");
    }

    private void castCleverImpersonator() {
        harness.castFromHand(player1, new CleverImpersonator(), "{2}{U}{U}");
    }

    @Test
    void mayDeclineToCopyAnAvailablePermanent() {
        harness.addToBattlefield(player2, new MarduSkullhunter());
        castCleverImpersonator();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Clever Impersonator");
        harness.assertInGraveyard(player1, "Clever Impersonator");
        harness.assertOnBattlefield(player2, "Mardu Skullhunter");
    }

    @Test
    void copiedEntryReplacementAppliesButSourceCountersAreNotCopied() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MarduSkullhunter());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        source.untap();
        castCleverImpersonator();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, source.getId());

        Permanent copy = impersonatorPermanent();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, copy)).isEqualTo(2);
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    void copiedCreatureEntryAbilityTriggers() {
        harness.addToBattlefield(player2, new SultaiSoothsayer());
        Island first = new Island();
        Island second = new Island();
        Island third = new Island();
        Island fourth = new Island();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        castCleverImpersonator();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Sultai Soothsayer"));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, third, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void copyingFaceDownCreatureCopiesItsFaceDownCharacteristics() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new WoollyLoxodon()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();
        Permanent source = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(source.isFaceDown()).isTrue();
        harness.forceActivePlayer(player1);
        castCleverImpersonator();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, source.getId());

        Permanent copy = impersonatorPermanent();
        assertThat(copy.isFaceDown()).isFalse();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, copy)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, copy)).isEqualTo(2);
        assertThat(copy.getCard().getName()).isNotEqualTo("Woolly Loxodon");
    }

    @Test
    void copyingPlaneswalkerEntersWithPrintedLoyaltyNotSourceCounters() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new SarkhanTheDragonspeaker());
        source.setCounterCount(CounterType.LOYALTY, 7);
        castCleverImpersonator();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(impersonatorPermanent().getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(source.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    void copyingAuraLetsControllerChooseANewLegalAttachment() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WoollyLoxodon());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WoollyLoxodon());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new DebilitatingInjury());
        aura.setAttachedTo(first.getId());
        castCleverImpersonator();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, aura.getId());
        harness.handlePermanentChosen(player1, second.getId());

        Permanent copy = impersonatorPermanent();
        assertThat(copy.getAttachedTo()).isEqualTo(second.getId());
        assertThat(aura.getAttachedTo()).isEqualTo(first.getId());
        assertThat(harness.getGameQueryService().getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, second)).isEqualTo(5);
    }

    private Permanent impersonatorPermanent() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Clever Impersonator"))
                .findFirst().orElseThrow();
    }
}

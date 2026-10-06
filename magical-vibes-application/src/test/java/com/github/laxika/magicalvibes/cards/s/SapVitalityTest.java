package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SapVitality.class, Forest.class, GrizzlyBears.class, NicolBolasPlaneswalker.class, Unsummon.class})
class SapVitalityTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SapVitality()));
        addSapVitalityMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void dealsThreeDamageToTargetPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new SapVitality()));
        addSapVitalityMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void perpetuallyBoostsChosenCreatureCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears creatureToBoost = new GrizzlyBears();
        harness.setHand(player1, List.of(new SapVitality(), creatureToBoost));
        addSapVitalityMana();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualPowerToughnessChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent boostedCreature = findPermanent(player1, "Grizzly Bears");
        assertThat(boostedCreature.getEffectivePower()).isEqualTo(5);
        assertThat(boostedCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void cannotTargetALand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SapVitality()));
        addSapVitalityMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    void resolvesWithoutAChoiceWhenHandContainsNoCreatureCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SapVitality(), new Forest()));
        addSapVitalityMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Sap Vitality");
    }

    @Test
    void onlyChosenCreatureReceivesBoostAndNoncreatureCannotBeChosen() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears unchosen = new GrizzlyBears();
        GrizzlyBears chosen = new GrizzlyBears();
        harness.setHand(player1, List.of(new SapVitality(), new Forest(), unchosen, chosen));
        addSapVitalityMana();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleCardChosen(player1, 2);
        harness.castCreature(player1, 1);
        harness.passBothPriorities();
        harness.castCreature(player1, 1);
        harness.passBothPriorities();

        Permanent unchosenPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(unchosen.getId()))
                .findFirst().orElseThrow();
        Permanent chosenPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(chosen.getId()))
                .findFirst().orElseThrow();
        assertThat(unchosenPermanent.getEffectivePower()).isEqualTo(2);
        assertThat(chosenPermanent.getEffectivePower()).isEqualTo(5);
        assertThat(chosenPermanent.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void repeatedResolutionsAccumulateOnTheSameCreatureCard() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SapVitality(), new SapVitality(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, firstTarget.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.castInstant(player1, 0, secondTarget.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent boostedCreature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(boostedCreature.getEffectivePower()).isEqualTo(8);
        assertThat(boostedCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void illegalTargetPreventsTheHandBoostFromResolving() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SapVitality(), new SapVitality(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent boostedCreature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(boostedCreature.getEffectivePower()).isEqualTo(5);
        assertThat(boostedCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void perpetualBoostSurvivesReturningToHandAndBeingRecast() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SapVitality(), new GrizzlyBears(), new Unsummon()));
        addSapVitalityMana();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent firstIncarnation = findPermanent(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, firstIncarnation.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent secondIncarnation = findPermanent(player1, "Grizzly Bears");
        assertThat(secondIncarnation.getId()).isNotEqualTo(firstIncarnation.getId());
        assertThat(secondIncarnation.getEffectivePower()).isEqualTo(5);
        assertThat(secondIncarnation.getEffectiveToughness()).isEqualTo(2);
    }

    private void addSapVitalityMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.c.CrystallizedSerah;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HopeEstheim;
import com.github.laxika.magicalvibes.cards.r.RydiaSummonerOfMist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerahFarron.class, CrystallizedSerah.class, AdelizTheCinderWind.class,
        ArvadTheCursed.class, CaptainSisay.class, GrizzlyBears.class,
        HopeEstheim.class, RydiaSummonerOfMist.class, SazhsChocobo.class})
class SerahFarronTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces only the first legendary creature spell each turn")
    void reducesOnlyFirstLegendaryCreatureSpellEachTurn() {
        addCreatureReady(player1, new SerahFarron());
        harness.setHand(player1, List.of(new AdelizTheCinderWind(), new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    @Test
    @DisplayName("A nonmatching spell does not use the reduction")
    void nonmatchingSpellDoesNotUseReduction() {
        addCreatureReady(player1, new SerahFarron());
        harness.setHand(player1, List.of(new GrizzlyBears(), new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("May transform at the beginning of combat with two other legendary creatures")
    void transformsWithTwoOtherLegendaryCreatures() {
        Permanent serah = addCreatureReady(player1, new SerahFarron());
        Permanent target = addCreatureReady(player1, new CaptainSisay());
        addCreatureReady(player1, new ArvadTheCursed());
        int powerBeforeTransform = gqs.getEffectivePower(gd, target);

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(serah.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(powerBeforeTransform + 2);
    }

    @Test
    @DisplayName("Does not offer transformation without two other legendary creatures")
    void doesNotTransformWithoutTwoOtherLegendaryCreatures() {
        Permanent serah = addCreatureReady(player1, new SerahFarron());
        addCreatureReady(player1, new ArvadTheCursed());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(serah.isTransformed()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @CardUsed({HopeEstheim.class, RydiaSummonerOfMist.class})
    void mayDeclineTransformation() {
        Permanent serah = addCreatureReady(player1, new SerahFarron());
        addCreatureReady(player1, new HopeEstheim());
        addCreatureReady(player1, new RydiaSummonerOfMist());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(serah.isTransformed()).isFalse();
    }

    @Test
    @CardUsed({HopeEstheim.class, RydiaSummonerOfMist.class, SazhsChocobo.class})
    void transformedFaceBoostsOnlyOwnLegendaryCreatures() {
        Permanent serah = addCreatureReady(player1, new SerahFarron());
        Permanent hope = addCreatureReady(player1, new HopeEstheim());
        addCreatureReady(player1, new RydiaSummonerOfMist());
        Permanent chocobo = addCreatureReady(player1, new SazhsChocobo());
        Permanent opposingHope = addCreatureReady(player2, new HopeEstheim());
        int ownPower = gqs.getEffectivePower(gd, hope);
        int ownToughness = gqs.getEffectiveToughness(gd, hope);
        int nonlegendaryPower = gqs.getEffectivePower(gd, chocobo);
        int nonlegendaryToughness = gqs.getEffectiveToughness(gd, chocobo);
        int opposingPower = gqs.getEffectivePower(gd, opposingHope);
        int opposingToughness = gqs.getEffectiveToughness(gd, opposingHope);

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(serah.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, hope)).isEqualTo(ownPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, hope)).isEqualTo(ownToughness + 2);
        assertThat(gqs.getEffectivePower(gd, chocobo)).isEqualTo(nonlegendaryPower);
        assertThat(gqs.getEffectiveToughness(gd, chocobo)).isEqualTo(nonlegendaryToughness);
        assertThat(gqs.getEffectivePower(gd, opposingHope)).isEqualTo(opposingPower);
        assertThat(gqs.getEffectiveToughness(gd, opposingHope)).isEqualTo(opposingToughness);
    }

    @Test
    @CardUsed({HopeEstheim.class, RydiaSummonerOfMist.class})
    void transformedFaceStillReducesFirstLegendaryCreatureSpell() {
        addCreatureReady(player1, new SerahFarron());
        addCreatureReady(player1, new HopeEstheim());
        addCreatureReady(player1, new RydiaSummonerOfMist());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new SerahFarron()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @CardUsed({HopeEstheim.class, SazhsChocobo.class})
    void nonlegendaryAndOpposingCreaturesDoNotMeetTransformCondition() {
        Permanent serah = addCreatureReady(player1, new SerahFarron());
        addCreatureReady(player1, new HopeEstheim());
        addCreatureReady(player1, new SazhsChocobo());
        addCreatureReady(player2, new HopeEstheim());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(serah.isTransformed()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @CardUsed({HopeEstheim.class, RydiaSummonerOfMist.class})
    void transformConditionIsRecheckedWhenAbilityResolves() {
        Permanent serah = addCreatureReady(player1, new SerahFarron());
        addCreatureReady(player1, new HopeEstheim());
        Permanent rydia = addCreatureReady(player1, new RydiaSummonerOfMist());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(rydia);
        harness.passBothPriorities();

        assertThat(serah.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @CardUsed({HopeEstheim.class})
    void legendarySpellCastBeforeSerahEnteredStillUsesFirstSpellAllowance() {
        harness.setHand(player1, List.of(new HopeEstheim()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        addCreatureReady(player1, new SerahFarron());
        harness.setHand(player1, List.of(new SerahFarron()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    @Test
    void doesNotReduceOpponentsLegendaryCreatureSpell() {
        addCreatureReady(player2, new SerahFarron());
        harness.setHand(player1, List.of(new SerahFarron()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    @Test
    @CardUsed({HopeEstheim.class, RydiaSummonerOfMist.class})
    void transformingDoesNotResetFirstLegendarySpellAllowance() {
        addCreatureReady(player1, new HopeEstheim());
        addCreatureReady(player1, new RydiaSummonerOfMist());
        harness.setHand(player1, List.of(new SerahFarron()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new SerahFarron()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    @Test
    @CardUsed({HopeEstheim.class})
    void reductionIsAvailableAgainOnANewTurn() {
        addCreatureReady(player1, new SerahFarron());
        harness.setHand(player1, List.of(new HopeEstheim()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SerahFarron()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }
}

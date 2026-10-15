package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.e.ExtinguishTheLight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VodalianMindsinger;
import com.github.laxika.magicalvibes.cards.w.WalkingBulwark;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TyrannicalPitlord.class, DiabolicEdict.class, GrizzlyBears.class,
        ExtinguishTheLight.class, VodalianMindsinger.class, WalkingBulwark.class})
class TyrannicalPitlordTest extends BaseCardTest {

    @Test
    void chosenCreatureGetsBoostAndFlying() {
        Permanent chosen = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        harness.castFromHand(player1, new TyrannicalPitlord(), "{4}{B}{B}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gqs.getEffectivePower(gd, chosen)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, chosen)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, chosen, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }

    @Test
    void leavingBattlefieldSacrificesChosenCreature() {
        Permanent chosen = addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new TyrannicalPitlord(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());
        Permanent pitlord = findPermanent(player1, "Tyrannical Pitlord");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.handlePermanentChosen(player1, pitlord.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tyrannical Pitlord");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void entersWithoutChoiceWhenYouControlNoOtherCreature() {
        harness.castFromHand(player1, new TyrannicalPitlord(), "{4}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice).isFalse();
        harness.assertOnBattlefield(player1, "Tyrannical Pitlord");
    }

    @Test
    void entryChoiceIncludesOnlyOtherCreaturesYouControl() {
        Permanent own = addCreatureReady(player1, new WalkingBulwark());
        addCreatureReady(player2, new WalkingBulwark());

        harness.castFromHand(player1, new TyrannicalPitlord(), "{4}{B}{B}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(own.getId());
        harness.handlePermanentChosen(player1, own.getId());
    }

    @Test
    void chosenCreatureControlledByOpponentKeepsBoostButIsNotSacrificed() {
        Permanent chosen = addCreatureReady(player1, new WalkingBulwark());
        harness.castFromHand(player1, new TyrannicalPitlord(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());
        Permanent pitlord = findPermanent(player1, "Tyrannical Pitlord");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new VodalianMindsinger()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castKickedCreature(player2, 0, chosen.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(chosen);
        assertThat(gqs.getEffectivePower(gd, chosen)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chosen)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, chosen, Keyword.FLYING)).isTrue();

        destroyPitlord(pitlord);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(chosen);
        assertThat(gqs.getEffectivePower(gd, chosen)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, chosen)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, chosen, Keyword.FLYING)).isFalse();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(chosen);
        harness.assertNotInGraveyard(player1, "Walking Bulwark");
    }

    @Test
    void sacrificingChosenPitlordAlsoSacrificesItsOwnChosenCreature() {
        Permanent chosen = addCreatureReady(player1, new WalkingBulwark());
        harness.castFromHand(player1, new TyrannicalPitlord(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());
        Permanent firstPitlord = findPermanent(player1, "Tyrannical Pitlord");

        TyrannicalPitlord secondCard = new TyrannicalPitlord();
        harness.castFromHand(player1, secondCard, "{4}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, firstPitlord.getId());
        Permanent secondPitlord = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == secondCard)
                .findFirst().orElseThrow();

        destroyPitlord(secondPitlord);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(firstPitlord, secondPitlord, chosen);
        harness.assertInGraveyard(player1, "Walking Bulwark");
    }

    private void destroyPitlord(Permanent pitlord) {
        harness.setHand(player1, List.of(new ExtinguishTheLight()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, pitlord.getId());
    }
}

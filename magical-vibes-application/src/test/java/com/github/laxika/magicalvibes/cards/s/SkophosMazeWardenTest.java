package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkophosMazeWarden.class, GrizzlyBears.class})
class SkophosMazeWardenTest extends BaseCardTest {

    @Test
    void boostsSelfUntilEndOfTurn() {
        Permanent warden = addWarden(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(4);
    }

    @Test
    void mayFightCreatureTargetedByLabyrinthAbility() {
        Permanent warden = addWarden(player1);
        addLabyrinth(player1, "Labyrinth of Skophos");
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(warden.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    void decliningFightLeavesTargetOnBattlefield() {
        addWarden(player1);
        addLabyrinth(player1, "Labyrinth of Skophos");
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    void onlyAnotherCreatureTargetedByLandYouControlTriggers() {
        Permanent warden = addWarden(player1);
        addLabyrinth(player1, "Other Land");
        Permanent labyrinth = addLabyrinth(player1, "Labyrinth of Skophos");
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));

        harness.activateAbility(player1, 2, null, warden.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(warden.isTapped()).isTrue();
        assertThat(labyrinth.isTapped()).isTrue();
    }

    @Test
    void doesNotTriggerForLabyrinthControlledByAnotherPlayer() {
        addWarden(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addLabyrinth(player2, "Labyrinth of Skophos");

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
    }

    private Permanent addWarden(Player player) {
        return addCreatureReady(player, new SkophosMazeWarden());
    }

    private Permanent addLabyrinth(Player player, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        card.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "Tap target creature.",
                TargetFilters.creature()
        ));
        Permanent land = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(land);
        return land;
    }
}

package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GisasFavoriteShovel.class, GrizzlyBears.class, SuntailHawk.class})
class GisasFavoriteShovelTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0 and menace")
    void equippedCreatureGetsBoostAndMenace() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shovel = addShovelReady(player1);
        shovel.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("When the equipped creature attacks, the defender sacrifices a creature and you create a Walker")
    void attackSacrificesDefendingCreatureAndCreatesWalker() {
        Permanent shovel = addShovelReady(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        shovel.setAttachedTo(attacker.getId());
        addCreatureReady(player2, new SuntailHawk());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        assertThat(countPermanents(player1, "Walker")).isOne();
    }

    @Test
    @DisplayName("The defending player chooses which creature to sacrifice")
    void defendingPlayerChoosesCreature() {
        Permanent shovel = addShovelReady(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        shovel.setAttachedTo(attacker.getId());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent hawk = addCreatureReady(player2, new SuntailHawk());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player2, List.of(hawk.getId()));

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Walker")).isOne();
    }

    @Test
    @DisplayName("Attacking a player with no creatures creates no Walker")
    void noDefendingCreatureCreatesNoWalker() {
        Permanent shovel = addShovelReady(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        shovel.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Walker")).isZero();
    }

    private Permanent addShovelReady(Player player) {
        Permanent shovel = new Permanent(new GisasFavoriteShovel());
        shovel.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(shovel);
        return shovel;
    }
}

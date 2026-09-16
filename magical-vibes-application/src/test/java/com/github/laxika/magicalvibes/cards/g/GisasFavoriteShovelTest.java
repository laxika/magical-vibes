package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GisasFavoriteShovel.class, GrizzlyBears.class})
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
    @DisplayName("Equip ability attaches Gisa's Favorite Shovel")
    void equipAttachesShovel() {
        Permanent shovel = addShovelReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shovel.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Attacking with the equipped creature sacrifices a defending creature and creates a Walker")
    void attackSacrificesDefendingCreatureAndCreatesWalker() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shovel = addShovelReady(player1);
        shovel.setAttachedTo(creature.getId());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Walker")).hasSize(1);
        Permanent walker = findPermanents(player1, "Walker").getFirst();
        assertThat(gqs.getEffectivePower(gd, walker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, walker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Defending player chooses which creature to sacrifice")
    void defendingPlayerChoosesCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shovel = addShovelReady(player1);
        shovel.setAttachedTo(creature.getId());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(second);
        assertThat(findPermanents(player1, "Walker")).hasSize(1);
    }

    @Test
    @DisplayName("No Walker is created when the defending player controls no creatures")
    void noCreatureMeansNoWalker() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shovel = addShovelReady(player1);
        shovel.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Walker")).isEmpty();
    }

    private Permanent addShovelReady(Player player) {
        Permanent shovel = new Permanent(new GisasFavoriteShovel());
        shovel.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(shovel);
        return shovel;
    }
}

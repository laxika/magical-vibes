package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LinvalaKeeperOfSilenceTest extends BaseCardTest {

    @Test
    @DisplayName("Blocks activated abilities of creatures opponents control")
    void blocksOpponentsCreatureAbilities() {
        harness.addToBattlefield(player1, new LinvalaKeeperOfSilence());
        addCreatureWithActivatedAbility(player2, "Prodigal Pyromancer");

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Linvala, Keeper of Silence");
    }

    @Test
    @DisplayName("Blocks mana abilities of creatures opponents control")
    void blocksOpponentsCreatureManaAbilities() {
        harness.addToBattlefield(player1, new LinvalaKeeperOfSilence());
        Permanent creature = addManaCreature(player2);

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Linvala, Keeper of Silence");
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Does not block activated abilities of creatures its controller controls")
    void allowsControllersCreatureAbilities() {
        harness.addToBattlefield(player1, new LinvalaKeeperOfSilence());
        addCreatureWithActivatedAbility(player1, "Prodigal Pyromancer");

        harness.activateAbility(player1, 1, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not block activated abilities of noncreature permanents")
    void allowsOpponentsNoncreatureAbilities() {
        harness.addToBattlefield(player1, new LinvalaKeeperOfSilence());
        addNoncreatureWithActivatedAbility(player2, "Seismic Assault");

        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addCreatureWithActivatedAbility(Player player, String name) {
        Card creature = new Card();
        creature.setName(name);
        creature.setType(CardType.CREATURE);
        creature.addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Deal 1 damage to any target."
        ));
        Permanent permanent = new Permanent(creature);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addManaCreature(Player player) {
        Card creature = new Card();
        creature.setName("Llanowar Elves");
        creature.setType(CardType.CREATURE);
        creature.addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
        Permanent permanent = new Permanent(creature);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addNoncreatureWithActivatedAbility(Player player, String name) {
        Card permanentCard = new Card();
        permanentCard.setName(name);
        permanentCard.setType(CardType.ENCHANTMENT);
        permanentCard.addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Deal 1 damage to any target."
        ));
        Permanent permanent = new Permanent(permanentCard);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

package com.github.laxika.magicalvibes.cards.c;

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

class CursedTotemTest extends BaseCardTest {

    @Test
    @DisplayName("Blocks non-mana activated abilities of creatures")
    void blocksCreatureActivatedAbilities() {
        addCursedTotem(player1);

        addCreatureWithActivatedAbility(player2, "Prodigal Pyromancer");

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Cursed Totem");
    }

    @Test
    @DisplayName("Blocks activated abilities of own creatures")
    void blocksOwnCreatureActivatedAbilities() {
        addCursedTotem(player1);

        addCreatureWithActivatedAbility(player1, "Prodigal Pyromancer");

        // Creature is at index 1 (after Cursed Totem at index 0)
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Cursed Totem");
    }

    @Test
    @DisplayName("Blocks mana abilities of creatures")
    void blocksCreatureManaAbilities() {
        addCursedTotem(player1);

        Card creature = new Card();
        creature.setName("Llanowar Elves");
        creature.setType(CardType.CREATURE);
        creature.addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
        Permanent perm = new Permanent(creature);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(perm);

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Cursed Totem");
    }

    @Test
    @DisplayName("Does NOT block mana abilities of non-creature permanents (lands)")
    void doesNotBlockLandManaAbilities() {
        addCursedTotem(player1);

        Card land = new Card();
        land.setName("Forest");
        land.setType(CardType.LAND);
        land.addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
        Permanent landPerm = new Permanent(land);
        gd.playerBattlefields.get(player2.getId()).add(landPerm);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing Cursed Totem re-enables creature abilities")
    void removingCursedTotemReenablesAbilities() {
        Permanent totem = addCursedTotem(player1);
        addCreatureWithActivatedAbility(player2, "Prodigal Pyromancer");

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        gd.playerBattlefields.get(player1.getId()).remove(totem);

        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Helpers =====

    private Permanent addCursedTotem(Player player) {
        CursedTotem card = new CursedTotem();
        card.setName("Cursed Totem");
        card.setType(CardType.ARTIFACT);
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureWithActivatedAbility(Player player, String name) {
        Card creature = new Card();
        creature.setName(name);
        creature.setType(CardType.CREATURE);
        creature.setPower(1);
        creature.setToughness(1);
        creature.addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Deal 1 damage to any target."
        ));
        Permanent perm = new Permanent(creature);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

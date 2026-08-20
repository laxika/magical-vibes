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

class ClarionConquerorTest extends BaseCardTest {

    @Test
    @DisplayName("Blocks activated abilities of artifacts, creatures, and planeswalkers")
    void blocksMatchingPermanentAbilities() {
        addClarionConqueror(player1);
        addActivatedAbilityPermanent(player2, CardType.ARTIFACT, "Ratchet Bomb");
        addActivatedAbilityPermanent(player2, CardType.CREATURE, "Prodigal Pyromancer");
        addActivatedAbilityPermanent(player2, CardType.PLANESWALKER, "Chandra");

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Clarion Conqueror");
        assertThatThrownBy(() -> harness.activateAbility(player2, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThatThrownBy(() -> harness.activateAbility(player2, 2, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Blocks mana abilities of artifacts and creatures")
    void blocksMatchingPermanentManaAbilities() {
        addClarionConqueror(player1);
        addManaPermanent(player2, CardType.ARTIFACT, "Sol Ring");
        addManaPermanent(player2, CardType.CREATURE, "Llanowar Elves").setSummoningSick(false);

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThatThrownBy(() -> harness.tapPermanent(player2, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not block activated abilities of lands")
    void allowsLandAbilities() {
        addClarionConqueror(player1);
        addActivatedAbilityPermanent(player2, CardType.LAND, "Hostile Desert");

        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Removing Clarion Conqueror re-enables matching abilities")
    void removalReenablesAbilities() {
        Permanent clarion = addClarionConqueror(player1);
        addActivatedAbilityPermanent(player2, CardType.CREATURE, "Prodigal Pyromancer");

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        gd.playerBattlefields.get(player1.getId()).remove(clarion);
        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addClarionConqueror(Player player) {
        ClarionConqueror card = new ClarionConqueror();
        card.setName("Clarion Conqueror");
        card.setType(CardType.CREATURE);
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addActivatedAbilityPermanent(Player player, CardType type, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Deal 1 damage to any target."
        ));
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addManaPermanent(Player player, CardType type, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.addEffect(EffectSlot.ON_TAP, new AwardManaEffect(
                type == CardType.ARTIFACT ? ManaColor.COLORLESS : ManaColor.GREEN
        ));
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

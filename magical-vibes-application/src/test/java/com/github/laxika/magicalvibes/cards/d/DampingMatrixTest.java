package com.github.laxika.magicalvibes.cards.d;

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

class DampingMatrixTest extends BaseCardTest {

    @Test
    @DisplayName("Blocks non-mana activated abilities of artifacts")
    void blocksArtifactActivatedAbilities() {
        addDampingMatrix(player1);
        addActivatedAbilityPermanent(player2, CardType.ARTIFACT, "Ratchet Bomb");

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Damping Matrix");
    }

    @Test
    @DisplayName("Blocks non-mana activated abilities of creatures")
    void blocksCreatureActivatedAbilities() {
        addDampingMatrix(player1);
        addActivatedAbilityPermanent(player2, CardType.CREATURE, "Prodigal Pyromancer");

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Damping Matrix");
    }

    @Test
    @DisplayName("Does not block artifact mana abilities")
    void allowsArtifactManaAbilities() {
        addDampingMatrix(player1);
        addManaPermanent(player2, CardType.ARTIFACT, "Sol Ring");

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not block creature mana abilities")
    void allowsCreatureManaAbilities() {
        addDampingMatrix(player1);
        Permanent manaCreature = addManaPermanent(player2, CardType.CREATURE, "Llanowar Elves");
        manaCreature.setSummoningSick(false);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not block directly activated artifact mana abilities")
    void allowsDirectlyActivatedArtifactManaAbilities() {
        addDampingMatrix(player1);
        Card artifact = new Card();
        artifact.setName("Mana Vault");
        artifact.setType(CardType.ARTIFACT);
        artifact.addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS, 3)),
                "{T}: Add {C}{C}{C}."
        ));
        Permanent permanent = new Permanent(artifact);
        gd.playerBattlefields.get(player2.getId()).add(permanent);

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not block activated abilities of permanents that are neither artifacts nor creatures")
    void allowsOtherPermanentActivatedAbilities() {
        addDampingMatrix(player1);
        addActivatedAbilityPermanent(player2, CardType.ENCHANTMENT, "Seismic Assault");

        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addDampingMatrix(Player player) {
        DampingMatrix card = new DampingMatrix();
        card.setName("Damping Matrix");
        card.setType(CardType.ARTIFACT);
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
                type == CardType.ARTIFACT ? ManaColor.COLORLESS : ManaColor.GREEN,
                type == CardType.ARTIFACT ? 2 : 1
        ));
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

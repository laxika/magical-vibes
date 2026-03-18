package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StonySilenceTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Stony Silence has correct static effect")
    void hasCorrectStaticEffect() {
        StonySilence card = new StonySilence();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect.class);

        var effect = (ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.predicate()).isInstanceOf(PermanentIsArtifactPredicate.class);
    }

    // ===== Blocking artifact mana abilities (tap for mana) =====

    @Test
    @DisplayName("Blocks mana abilities of artifacts")
    void blocksManaAbilitiesOfArtifacts() {
        addStonySilence(player1);

        Permanent artifact = addArtifactWithManaAbility(player2, "Sol Ring");

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Stony Silence");
    }

    @Test
    @DisplayName("Blocks mana abilities of own artifacts")
    void blocksManaAbilitiesOfOwnArtifacts() {
        addStonySilence(player1);

        Permanent artifact = addArtifactWithManaAbility(player1, "Sol Ring");

        // Sol Ring is at index 1 (after Stony Silence at index 0)
        assertThatThrownBy(() -> harness.tapPermanent(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Stony Silence");
    }

    // ===== Blocking artifact activated abilities =====

    @Test
    @DisplayName("Blocks non-mana activated abilities of artifacts")
    void blocksActivatedAbilitiesOfArtifacts() {
        addStonySilence(player1);

        Permanent artifact = addArtifactWithActivatedAbility(player2, "Ratchet Bomb");

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Stony Silence");
    }

    // ===== Does NOT block non-artifact permanents =====

    @Test
    @DisplayName("Does NOT block mana abilities of non-artifact permanents (lands)")
    void doesNotBlockLandManaAbilities() {
        addStonySilence(player1);

        Card land = new Card();
        land.setName("Forest");
        land.setType(CardType.LAND);
        land.addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
        Permanent landPerm = new Permanent(land);
        gd.playerBattlefields.get(player2.getId()).add(landPerm);

        // Should work fine — lands are not artifacts
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does NOT block activated abilities of non-artifact creatures")
    void doesNotBlockCreatureActivatedAbilities() {
        addStonySilence(player1);

        Card creature = new Card();
        creature.setName("Prodigal Pyromancer");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{2}{R}");
        creature.setColor(CardColor.RED);
        creature.setPower(1);
        creature.setToughness(1);
        creature.addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Prodigal Pyromancer deals 1 damage to any target."
        ));
        Permanent creaturePerm = new Permanent(creature);
        creaturePerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(creaturePerm);

        // Should work fine — creatures are not artifacts
        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Blocks artifact creatures =====

    @Test
    @DisplayName("Blocks activated abilities of artifact creatures")
    void blocksArtifactCreatureAbilities() {
        addStonySilence(player1);

        Card artifactCreature = new Card();
        artifactCreature.setName("Steel Overseer");
        artifactCreature.setType(CardType.CREATURE);
        artifactCreature.setAdditionalTypes(Set.of(CardType.ARTIFACT));
        artifactCreature.setManaCost("{2}");
        artifactCreature.setPower(1);
        artifactCreature.setToughness(1);
        artifactCreature.addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Deal 1 damage to any target."
        ));
        Permanent creaturePerm = new Permanent(artifactCreature);
        creaturePerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(creaturePerm);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Stony Silence");
    }

    // ===== Removal re-enables abilities =====

    @Test
    @DisplayName("Removing Stony Silence re-enables artifact abilities")
    void removingStonySilenceReenablesAbilities() {
        Permanent silence = addStonySilence(player1);
        Permanent artifact = addArtifactWithManaAbility(player2, "Sol Ring");

        // Verify blocked
        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        // Remove Stony Silence
        gd.playerBattlefields.get(player1.getId()).remove(silence);

        // Now artifact abilities should work
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    // ===== Multiple Stony Silences =====

    @Test
    @DisplayName("Multiple Stony Silences still block (removing one leaves the other)")
    void multipleStonysilencesStillBlock() {
        Permanent silence1 = addStonySilence(player1);
        Permanent silence2 = addStonySilence(player2);
        Permanent artifact = addArtifactWithManaAbility(player2, "Sol Ring");

        // Remove one Stony Silence
        gd.playerBattlefields.get(player1.getId()).remove(silence1);

        // Still blocked by the other
        assertThatThrownBy(() -> harness.tapPermanent(player2, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    // ===== Stony Silence does not affect itself =====

    @Test
    @DisplayName("Stony Silence is an enchantment, not affected by its own ability")
    void stonySilenceIsNotAnArtifact() {
        StonySilence card = new StonySilence();

        // Stony Silence is an enchantment — it should never be treated as an artifact
        assertThat(card.getType()).isNotEqualTo(CardType.ARTIFACT);
    }

    // ===== Helpers =====

    private Permanent addStonySilence(Player player) {
        StonySilence card = new StonySilence();
        card.setType(CardType.ENCHANTMENT);
        card.setName("Stony Silence");
        card.setManaCost("{1}{W}");
        card.setColor(CardColor.WHITE);
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addArtifactWithManaAbility(Player player, String name) {
        Card artifact = new Card();
        artifact.setName(name);
        artifact.setType(CardType.ARTIFACT);
        artifact.setManaCost("{1}");
        artifact.addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS, 2));
        Permanent perm = new Permanent(artifact);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addArtifactWithActivatedAbility(Player player, String name) {
        Card artifact = new Card();
        artifact.setName(name);
        artifact.setType(CardType.ARTIFACT);
        artifact.setManaCost("{2}");
        artifact.addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Deal 1 damage to any target."
        ));
        Permanent perm = new Permanent(artifact);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VaultSkywardTest extends BaseCardTest {

    @Test
    @DisplayName("Vault Skyward has correct effects")
    void hasCorrectEffects() {
        VaultSkyward card = new VaultSkyward();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect grantEffect = (GrantKeywordEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(grantEffect.keyword()).isEqualTo(Keyword.FLYING);
        assertThat(grantEffect.scope()).isEqualTo(GrantScope.TARGET);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(UntapTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Resolving Vault Skyward grants flying and untaps target creature")
    void grantsFlightAndUntaps() {
        Permanent target = addTappedCreature(player2);
        harness.setHand(player1, List.of(new VaultSkyward()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flying granted by Vault Skyward expires at end of turn")
    void flyingExpiresAtEndOfTurn() {
        Permanent target = addTappedCreature(player2);
        harness.setHand(player1, List.of(new VaultSkyward()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        Permanent ownCreature = addTappedCreature(player1);
        harness.setHand(player1, List.of(new VaultSkyward()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(ownCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addTappedCreature(player1); // valid target so spell is playable
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        harness.setHand(player1, List.of(new VaultSkyward()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Vault Skyward fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addTappedCreature(player2);
        harness.setHand(player1, List.of(new VaultSkyward()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    private Permanent addTappedCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.tap();
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PristineAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Untapped Pristine Angel has protection from colors")
    void untappedAngelHasProtectionFromColors() {
        Permanent angel = addAngel(player2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, angel.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Untapped Pristine Angel has protection from artifacts")
    void untappedAngelHasProtectionFromArtifacts() {
        Permanent angel = addAngel(player2);
        harness.setHand(player1, List.of(createArtifactDamageSpell()));

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, angel.getId(), null))
                .isInstanceOf(IllegalStateException.class);

        angel.tap();
        gs.playCard(gd, player1, 0, 0, angel.getId(), null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Tapped Pristine Angel can be targeted by a colored spell")
    void tappedAngelLosesProtectionFromColors() {
        Permanent angel = addAngel(player2);
        angel.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, angel.getId(), null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting a spell may untap Pristine Angel")
    void castingSpellMayUntapAngel() {
        Permanent angel = addAngel(player1);
        angel.tap();
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(angel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining Pristine Angel's trigger leaves it tapped")
    void decliningUntapLeavesAngelTapped() {
        Permanent angel = addAngel(player1);
        angel.tap();
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(angel.isTapped()).isTrue();
    }

    private Permanent addAngel(Player player) {
        Permanent angel = new Permanent(new PristineAngel());
        angel.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(angel);
        return angel;
    }

    private Card createArtifactDamageSpell() {
        Card card = new Card();
        card.setName("Artifact Bolt");
        card.setType(CardType.INSTANT);
        card.setAdditionalTypes(java.util.Set.of(CardType.ARTIFACT));
        card.setManaCost("{0}");
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }
}

package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Demolish;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gravelighter;
import com.github.laxika.magicalvibes.cards.k.KamisFlare;
import com.github.laxika.magicalvibes.cards.l.LethalExploit;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CracklingEmergence.class, Demolish.class, Forest.class, Gravelighter.class, KamisFlare.class, LethalExploit.class})
class CracklingEmergenceTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land becomes a 3/3 red Spirit creature with haste and remains a land")
    void animatesEnchantedLand() {
        Permanent forest = addEnchantedForest();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, forest)).containsExactly(CardColor.RED);
        assertThat(gqs.computeStaticBonus(gd, forest).grantedSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("When enchanted land would be destroyed, the Aura is sacrificed and the land gains indestructible")
    void sacrificesAuraAndGrantsIndestructible() {
        Permanent forest = addEnchantedForest();

        harness.setHand(player2, List.of(new Demolish()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, forest.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Crackling Emergence");
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    @DisplayName("Crackling Emergence can enchant only a land controlled by its caster")
    void targetMustBeLandYouControl() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Permanent opponentForest = findPermanent(player2, "Forest");
        harness.setHand(player1, List.of(new CracklingEmergence()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentForest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land you control");
    }

    @Test
    @DisplayName("Casting the Aura on your land resolves and animates it")
    void resolvesOnOwnLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CracklingEmergence()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Crackling Emergence").getAttachedTo()).isEqualTo(forest.getId());
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lethal damage sacrifices the Aura without destroying or tapping the land")
    void replacesLethalDamage() {
        Permanent forest = addEnchantedForest();
        harness.setHand(player2, List.of(new KamisFlare()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, forest.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Crackling Emergence");
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(forest.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Zero toughness puts the land and Aura into the graveyard without replacement")
    void doesNotReplaceZeroToughness() {
        Permanent forest = addEnchantedForest();
        harness.setHand(player2, List.of(new LethalExploit(), new LethalExploit()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castInstant(player2, 0, forest.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Crackling Emergence");
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(1);

        harness.castInstant(player2, 0, forest.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Crackling Emergence");
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Granted indestructible lasts through the turn and expires before the next turn")
    void indestructibleExpiresAtEndOfTurn() {
        Permanent forest = addEnchantedForest();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Demolish(), new Demolish()));
        harness.addMana(player2, ManaColor.RED, 8);

        harness.castSorcery(player2, 0, forest.getId());
        harness.passBothPriorities();
        harness.castSorcery(player2, 0, forest.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing the enchanted land is not replaced by sacrificing the Aura")
    void doesNotReplaceSacrifice() {
        addEnchantedForest();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Gravelighter()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Crackling Emergence");
        harness.assertInGraveyard(player2, "Gravelighter");
    }

    @Test
    @DisplayName("The Aura falls off when its enchanted land changes controller")
    void auraCannotRemainOnOpponentsLand() {
        Permanent forest = addEnchantedForest();
        gd.playerBattlefields.get(player1.getId()).remove(forest);
        gd.playerBattlefields.get(player2.getId()).add(forest);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player1, "Crackling Emergence");
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Permanent addEnchantedForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CracklingEmergence());
        aura.setAttachedTo(forest.getId());
        return forest;
    }
}

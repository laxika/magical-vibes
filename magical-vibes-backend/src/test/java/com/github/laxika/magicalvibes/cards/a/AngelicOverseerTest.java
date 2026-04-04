package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AngelicOverseerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Angelic Overseer has correct static effects")
    void hasCorrectProperties() {
        AngelicOverseer card = new AngelicOverseer();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);

        var effect0 = (ControlsSubtypeConditionalEffect) card.getEffects(EffectSlot.STATIC).get(0);
        assertThat(effect0.subtype()).isEqualTo(CardSubtype.HUMAN);
        assertThat(effect0.wrapped()).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect hexproof = (GrantKeywordEffect) effect0.wrapped();
        assertThat(hexproof.keywords()).containsExactly(Keyword.HEXPROOF);
        assertThat(hexproof.scope()).isEqualTo(GrantScope.SELF);

        var effect1 = (ControlsSubtypeConditionalEffect) card.getEffects(EffectSlot.STATIC).get(1);
        assertThat(effect1.subtype()).isEqualTo(CardSubtype.HUMAN);
        assertThat(effect1.wrapped()).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect indestructible = (GrantKeywordEffect) effect1.wrapped();
        assertThat(indestructible.keywords()).containsExactly(Keyword.INDESTRUCTIBLE);
        assertThat(indestructible.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Conditional hexproof and indestructible with Human =====

    @Test
    @DisplayName("Angelic Overseer has hexproof and indestructible when controller controls a Human")
    void hasHexproofAndIndestructibleWithHuman() {
        harness.addToBattlefield(player1, new AngelicOverseer());
        harness.addToBattlefield(player1, new EliteVanguard()); // Human Soldier

        Permanent overseer = findPermanent(player1, "Angelic Overseer");

        assertThat(gqs.hasKeyword(gd, overseer, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Angelic Overseer does NOT have hexproof or indestructible without a Human")
    void noHexproofOrIndestructibleWithoutHuman() {
        harness.addToBattlefield(player1, new AngelicOverseer());
        harness.addToBattlefield(player1, new GrizzlyBears()); // Bear, not Human

        Permanent overseer = findPermanent(player1, "Angelic Overseer");

        assertThat(gqs.hasKeyword(gd, overseer, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Angelic Overseer alone does NOT have hexproof or indestructible")
    void noHexproofOrIndestructibleAlone() {
        harness.addToBattlefield(player1, new AngelicOverseer());

        Permanent overseer = findPermanent(player1, "Angelic Overseer");

        assertThat(gqs.hasKeyword(gd, overseer, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    // ===== Loses keywords when Human leaves =====

    @Test
    @DisplayName("Angelic Overseer loses hexproof and indestructible when the Human leaves the battlefield")
    void losesKeywordsWhenHumanLeaves() {
        harness.addToBattlefield(player1, new AngelicOverseer());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent overseer = findPermanent(player1, "Angelic Overseer");

        assertThat(gqs.hasKeyword(gd, overseer, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.INDESTRUCTIBLE)).isTrue();

        // Remove the Human from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Elite Vanguard"));

        // Keywords should be gone immediately (computed on the fly)
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    // ===== Opponent's Humans don't count =====

    @Test
    @DisplayName("Opponent's Human does not grant hexproof or indestructible")
    void opponentHumanDoesNotCount() {
        harness.addToBattlefield(player1, new AngelicOverseer());
        harness.addToBattlefield(player2, new EliteVanguard()); // Opponent's Human

        Permanent overseer = findPermanent(player1, "Angelic Overseer");

        assertThat(gqs.hasKeyword(gd, overseer, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    // ===== Hexproof prevents opponent targeting =====

    @Test
    @DisplayName("Opponent cannot target Angelic Overseer with spells when it has hexproof")
    void opponentCannotTargetWithHexproof() {
        harness.addToBattlefield(player1, new AngelicOverseer());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent overseer = findPermanent(player1, "Angelic Overseer");

        // Verify hexproof
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.HEXPROOF)).isTrue();

        // Player2 tries to Shock the Angelic Overseer
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, overseer.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Opponent can target Angelic Overseer when it does NOT have hexproof")
    void opponentCanTargetWithoutHexproof() {
        harness.addToBattlefield(player1, new AngelicOverseer());
        // No Human — no hexproof

        Permanent overseer = findPermanent(player1, "Angelic Overseer");
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.HEXPROOF)).isFalse();

        // Player2 can target with Shock
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        // Should not throw — targeting is allowed without hexproof
        gs.playCard(gd, player2, 0, 0, overseer.getId(), null);
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Indestructible prevents destroy effects =====

    @Test
    @DisplayName("Indestructible Angelic Overseer survives Wrath of God")
    void indestructibleSurvivesWrathOfGod() {
        harness.addToBattlefield(player1, new AngelicOverseer());
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent overseer = findPermanent(player1, "Angelic Overseer");
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.INDESTRUCTIBLE)).isTrue();

        // Cast Wrath of God
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        // Angelic Overseer should survive (indestructible at time of Wrath resolving)
        // Note: The Human (Elite Vanguard) dies to Wrath, but the Overseer was indestructible
        // when Wrath resolved because the Human was still on the battlefield at that moment.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angelic Overseer"));

        // Elite Vanguard should be destroyed
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Elite Vanguard"));

        // Opponent's bears should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Static bonus survives end-of-turn reset =====

    @Test
    @DisplayName("Static hexproof and indestructible survive end-of-turn modifier reset")
    void staticKeywordsSurviveEndOfTurnReset() {
        harness.addToBattlefield(player1, new AngelicOverseer());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent overseer = findPermanent(player1, "Angelic Overseer");

        assertThat(gqs.hasKeyword(gd, overseer, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.INDESTRUCTIBLE)).isTrue();

        // Simulate end-of-turn cleanup
        overseer.resetModifiers();

        // Static keywords should still be computed
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    // ===== Multiple Humans =====

    @Test
    @DisplayName("Losing one Human while controlling another still grants hexproof and indestructible")
    void stillProtectedWithMultipleHumans() {
        harness.addToBattlefield(player1, new AngelicOverseer());
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent overseer = findPermanent(player1, "Angelic Overseer");

        assertThat(gqs.hasKeyword(gd, overseer, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.INDESTRUCTIBLE)).isTrue();

        // Remove one Human
        Permanent firstVanguard = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elite Vanguard"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(firstVanguard);

        // Still has a Human — keywords remain
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, overseer, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    // ===== Helper methods =====

}

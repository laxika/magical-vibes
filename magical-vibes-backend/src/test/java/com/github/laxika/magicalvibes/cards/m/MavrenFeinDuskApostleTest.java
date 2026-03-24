package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.HasNontokenSubtypeAttackerConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MavrenFeinDuskApostleTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_ALLY_CREATURES_ATTACK with HasNontokenSubtypeAttackerConditionalEffect wrapping CreateTokenEffect")
    void hasCorrectAttackTrigger() {
        MavrenFeinDuskApostle card = new MavrenFeinDuskApostle();

        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURES_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURES_ATTACK).getFirst())
                .isInstanceOf(HasNontokenSubtypeAttackerConditionalEffect.class);

        HasNontokenSubtypeAttackerConditionalEffect conditional =
                (HasNontokenSubtypeAttackerConditionalEffect) card.getEffects(EffectSlot.ON_ALLY_CREATURES_ATTACK).getFirst();
        assertThat(conditional.requiredSubtype()).isEqualTo(CardSubtype.VAMPIRE);
        assertThat(conditional.wrapped()).isInstanceOf(CreateTokenEffect.class);

        CreateTokenEffect token = (CreateTokenEffect) conditional.wrapped();
        assertThat(token.tokenName()).isEqualTo("Vampire");
        assertThat(token.power()).isEqualTo(1);
        assertThat(token.toughness()).isEqualTo(1);
        assertThat(token.color()).isEqualTo(CardColor.WHITE);
        assertThat(token.subtypes()).containsExactly(CardSubtype.VAMPIRE);
        assertThat(token.keywords()).containsExactly(Keyword.LIFELINK);
    }

    // ===== Trigger: nontoken Vampire attacks =====

    @Test
    @DisplayName("Creates a Vampire token when a nontoken Vampire attacks")
    void createsTokenWhenNontokenVampireAttacks() {
        addMavrenFeinReady(player1);
        addVampireCreatureReady(player1);

        declareAttackers(List.of(1)); // index 1 is the Vampire creature
        harness.passBothPriorities(); // resolve attack trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vampire")
                        && p.getCard().isToken()
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1);
    }

    @Test
    @DisplayName("Creates only one token even when multiple nontoken Vampires attack")
    void createsOneTokenForMultipleVampireAttackers() {
        addMavrenFeinReady(player1);
        addVampireCreatureReady(player1);
        addVampireCreatureReady(player1);

        declareAttackers(List.of(1, 2)); // both Vampires attack
        harness.passBothPriorities(); // resolve attack trigger

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vampire") && p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates token when Mavren Fein himself attacks (he is a Vampire)")
    void createsTokenWhenMavrenFeinAttacks() {
        Permanent mavren = addMavrenFeinReady(player1);

        declareAttackers(List.of(0)); // Mavren Fein attacks
        harness.passBothPriorities(); // resolve attack trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vampire")
                        && p.getCard().isToken());
    }

    // ===== No trigger: non-Vampire or token attackers =====

    @Test
    @DisplayName("Does not trigger when only non-Vampire creatures attack")
    void doesNotTriggerForNonVampireAttackers() {
        addMavrenFeinReady(player1);
        addNonVampireCreatureReady(player1);

        declareAttackers(List.of(1)); // non-Vampire attacks

        // No trigger should be on the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when only Vampire tokens attack")
    void doesNotTriggerForVampireTokenAttackers() {
        addMavrenFeinReady(player1);
        addVampireTokenReady(player1);

        declareAttackers(List.of(1)); // Vampire token attacks

        // No trigger should be on the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers when a mix of nontoken Vampire and non-Vampire creatures attack")
    void triggersWithMixedAttackers() {
        addMavrenFeinReady(player1);
        addVampireCreatureReady(player1);
        addNonVampireCreatureReady(player1);

        declareAttackers(List.of(1, 2)); // Vampire and non-Vampire attack
        harness.passBothPriorities(); // resolve attack trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vampire")
                        && p.getCard().isToken());
    }

    @Test
    @DisplayName("Triggers when a nontoken Vampire attacks alongside a Vampire token")
    void triggersWithNontokenAndTokenVampires() {
        addMavrenFeinReady(player1);
        addVampireCreatureReady(player1);
        addVampireTokenReady(player1);

        declareAttackers(List.of(1, 2)); // nontoken Vampire + Vampire token attack
        harness.passBothPriorities(); // resolve attack trigger

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vampire") && p.getCard().isToken())
                .count();
        // 1 original Vampire token + 1 new token from trigger = 2
        assertThat(tokenCount).isEqualTo(2);
    }

    // ===== Helpers =====

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player1, attackerIndices);
    }

    private Permanent addMavrenFeinReady(Player player) {
        MavrenFeinDuskApostle card = new MavrenFeinDuskApostle();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addVampireCreatureReady(Player player) {
        Card creature = new Card();
        creature.setName("Test Vampire");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{B}");
        creature.setColor(CardColor.BLACK);
        creature.setSubtypes(List.of(CardSubtype.VAMPIRE));
        creature.setPower(2);
        creature.setToughness(2);
        Permanent perm = new Permanent(creature);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addVampireTokenReady(Player player) {
        Card tokenCard = new Card();
        tokenCard.setName("Vampire");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setColor(CardColor.WHITE);
        tokenCard.setSubtypes(List.of(CardSubtype.VAMPIRE));
        tokenCard.setPower(1);
        tokenCard.setToughness(1);
        tokenCard.setToken(true);
        Permanent perm = new Permanent(tokenCard);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addNonVampireCreatureReady(Player player) {
        Card creature = new Card();
        creature.setName("Test Soldier");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{W}");
        creature.setColor(CardColor.WHITE);
        creature.setSubtypes(List.of(CardSubtype.SOLDIER));
        creature.setPower(2);
        creature.setToughness(2);
        Permanent perm = new Permanent(creature);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

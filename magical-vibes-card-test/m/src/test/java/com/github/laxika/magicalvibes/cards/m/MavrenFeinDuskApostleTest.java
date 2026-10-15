package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.cards.l.LoomingAltisaur;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MavrenFeinDuskApostle.class, QueensBaySoldier.class, LoomingAltisaur.class})
class MavrenFeinDuskApostleTest extends BaseCardTest {

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
        addMavrenFeinReady(player1);

        declareAttackers(List.of(0)); // Mavren Fein attacks
        harness.passBothPriorities(); // resolve attack trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vampire")
                        && p.getCard().isToken());
    }

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

    @Test
    @DisplayName("Token has the specified characteristics and enters without attacking")
    void createsWhiteVampireWithLifelinkNotAttacking() {
        addMavrenFeinReady(player1);
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Vampire");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.VAMPIRE);
        assertThat(token.getCard().getKeywords()).contains(Keyword.LIFELINK);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.isAttacking()).isFalse();
        assertThat(token.isTapped()).isFalse();
        assertThat(countPermanents(player2, "Vampire")).isZero();
    }

    @Test
    @DisplayName("Trigger still creates a token after its attacking Vampire leaves")
    void createsTokenAfterAttackerLeaves() {
        addMavrenFeinReady(player1);
        Permanent attacker = addVampireCreatureReady(player1);
        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Vampire")).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's attacking Vampire does not trigger Mavren")
    void doesNotTriggerForOpponentVampire() {
        addMavrenFeinReady(player1);
        addVampireCreatureReady(player2);
        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Vampire")).isZero();
    }

    @Test
    @DisplayName("Trigger still creates a token after Mavren leaves")
    void createsTokenAfterMavrenLeaves() {
        Permanent mavren = addMavrenFeinReady(player1);
        addVampireCreatureReady(player1);
        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(mavren);
        gd.playerGraveyards.get(player1.getId()).add(mavren.getCard());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Vampire")).isEqualTo(1);
    }

    private Permanent addMavrenFeinReady(Player player) {
        return addCreatureReady(player, new MavrenFeinDuskApostle());
    }

    private Permanent addVampireCreatureReady(Player player) {
        return addCreatureReady(player, new QueensBaySoldier());
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
        return addCreatureReady(player, tokenCard);
    }

    private Permanent addNonVampireCreatureReady(Player player) {
        return addCreatureReady(player, new LoomingAltisaur());
    }
}

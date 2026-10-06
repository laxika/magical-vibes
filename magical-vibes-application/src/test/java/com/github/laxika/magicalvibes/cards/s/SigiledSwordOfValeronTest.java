package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigiledSwordOfValeron.class, WalkingCorpse.class})
class SigiledSwordOfValeronTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0, vigilance, and Knight subtype")
    void equippedCreatureGetsGrants() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SigiledSwordOfValeron());
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, creature).grantedSubtypes()).contains(CardSubtype.KNIGHT);
    }

    @Test
    @DisplayName("Equipped creature's attack creates an untapped and attacking vigilant Knight")
    void attackTriggerCreatesAttackingKnight() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SigiledSwordOfValeron());
        sword.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Knight");
        assertThat(knight.isTapped()).isFalse();
        assertThat(knight.isAttacking()).isTrue();
        assertThat(knight.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(creature.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Knight")).isEqualTo(1);
        assertThat(knight.getCard().getSubtypes()).contains(CardSubtype.KNIGHT);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The attack trigger does not fire while the Sword is unattached")
    void noTriggerWhenUnattached() {
        addCreatureReady(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new SigiledSwordOfValeron());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Sigiled Sword of Valeron"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Knight"));
    }

    @Test
    @DisplayName("Equip {3} attaches the Sword to a creature you control")
    void equipAttachesToCreature() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SigiledSwordOfValeron());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Re-equipping moves all grants and preserves the creatures' original types")
    void reequippingMovesGrants() {
        Permanent first = addCreatureReady(player1, new WalkingCorpse());
        Permanent second = addCreatureReady(player1, new WalkingCorpse());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SigiledSwordOfValeron());
        sword.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, first, CardSubtype.KNIGHT)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, second, CardSubtype.KNIGHT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, second, CardSubtype.ZOMBIE)).isTrue();
    }

    @Test
    @DisplayName("Only the equipped creature's attack triggers the Sword")
    void attackingOtherCreatureDoesNotTrigger() {
        Permanent equipped = addCreatureReady(player1, new WalkingCorpse());
        addCreatureReady(player1, new WalkingCorpse());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SigiledSwordOfValeron());
        sword.setAttachedTo(equipped.getId());

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Knight")).isZero();
    }

    @Test
    @DisplayName("The Sword's controller creates an untapped nonattacking Knight on an opponent's turn")
    void opponentControlsEquippedCreature() {
        Permanent creature = addCreatureReady(player2, new WalkingCorpse());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SigiledSwordOfValeron());
        sword.setAttachedTo(creature.getId());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Knight");
        assertThat(knight.isTapped()).isFalse();
        assertThat(knight.isAttacking()).isFalse();
        assertThat(countPermanents(player2, "Knight")).isZero();
    }

    @Test
    @DisplayName("An attack trigger still creates its Knight after the Sword leaves")
    void triggerResolvesAfterSwordLeaves() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SigiledSwordOfValeron());
        sword.setAttachedTo(creature.getId());
        declareAttackers(player1, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(sword);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Knight")).isEqualTo(1);
        assertThat(findPermanent(player1, "Knight").isAttacking()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

}

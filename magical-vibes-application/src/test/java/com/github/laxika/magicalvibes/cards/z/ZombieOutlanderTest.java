package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FieryFall;
import com.github.laxika.magicalvibes.cards.m.MightOfAlara;
import com.github.laxika.magicalvibes.cards.o.Oakenform;
import com.github.laxika.magicalvibes.cards.r.RhoxBodyguard;
import com.github.laxika.magicalvibes.cards.a.AshasFavor;
import com.github.laxika.magicalvibes.cards.b.BeaconBehemoth;
import com.github.laxika.magicalvibes.cards.n.NacatlHuntPride;
import com.github.laxika.magicalvibes.cards.s.ScattershotArcher;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZombieOutlander.class, GrizzlyBears.class, FieryFall.class, MightOfAlara.class,
        Oakenform.class, RhoxBodyguard.class, AshasFavor.class, NacatlHuntPride.class,
        ScattershotArcher.class, BeaconBehemoth.class})
class ZombieOutlanderTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card createTargetedInstant(String name, CardColor color, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }

    @Test
    @DisplayName("Green creature cannot block Zombie Outlander")
    void greenCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new ZombieOutlander());
        attacker.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Black creature can block Zombie Outlander")
    void blackCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new ZombieOutlander());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, createCreature("Black Knight", 2, 2, CardColor.BLACK));

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Zombie Outlander takes no combat damage from green creature")
    void takesNoDamageFromGreen() {
        // Green 3/3 attacker, Zombie Outlander as blocker
        Permanent attacker = addCreatureReady(player1, createCreature("Big Green", 3, 3, CardColor.GREEN));
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new ZombieOutlander());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Green creature's 3 damage to the Outlander is prevented (protection); Outlander survives
        harness.assertOnBattlefield(player2, "Zombie Outlander");
    }

    @Test
    @DisplayName("Zombie Outlander takes normal combat damage from black creature")
    void takesNormalDamageFromBlack() {
        // Black 3/3 attacker, Zombie Outlander as blocker
        Permanent attacker = addCreatureReady(player1, createCreature("Black Knight", 3, 3, CardColor.BLACK));
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new ZombieOutlander());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Black creature's 3 damage kills the 2/2 Outlander with no protection from black
        harness.assertNotOnBattlefield(player2, "Zombie Outlander");
        harness.assertInGraveyard(player2, "Zombie Outlander");
    }

    @Test
    @DisplayName("Cannot be targeted by green instant")
    void cannotBeTargetedByGreenInstant() {
        Permanent outlander = addCreatureReady(player2, new ZombieOutlander());

        // Add a valid target so the spell is playable
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(createTargetedInstant("Green Bolt", CardColor.GREEN, "{G}")));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, outlander.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    @DisplayName("Can be targeted by red instant")
    void canBeTargetedByRedInstant() {
        Permanent outlander = addCreatureReady(player1, new ZombieOutlander());

        harness.setHand(player1, List.of(createTargetedInstant("Red Bolt", CardColor.RED, "{R}")));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, outlander.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Red Bolt");
    }

    @Test
    @DisplayName("A green and white creature cannot block Zombie Outlander")
    void multicoloredGreenCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new ZombieOutlander());
        attacker.setAttacking(true);
        addCreatureReady(player2, new RhoxBodyguard());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection also forbids its controller's green combat trick")
    void cannotTargetOwnOutlanderWithGreenSpell() {
        Permanent outlander = addCreatureReady(player1, new ZombieOutlander());
        addCreatureReady(player1, new RhoxBodyguard());
        harness.setHand(player1, List.of(new MightOfAlara()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, outlander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Red spell damage is not prevented")
    void redSpellDealsLethalDamage() {
        Permanent outlander = addCreatureReady(player2, new ZombieOutlander());
        harness.setHand(player1, List.of(new FieryFall()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, outlander.getId());

        harness.assertNotOnBattlefield(player2, "Zombie Outlander");
        harness.assertInGraveyard(player2, "Zombie Outlander");
    }

    @Test
    @DisplayName("A green Aura cannot target Zombie Outlander")
    void greenAuraCannotTarget() {
        Permanent outlander = addCreatureReady(player1, new ZombieOutlander());
        addCreatureReady(player1, new RhoxBodyguard());
        harness.setHand(player1, List.of(new Oakenform()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, outlander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("An attached green Aura is put into its owner's graveyard")
    void attachedGreenAuraIsRemovedByStateBasedActions() {
        Permanent outlander = addCreatureReady(player1, new ZombieOutlander());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Oakenform());
        aura.setAttachedTo(outlander.getId());

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Zombie Outlander");
        harness.assertNotOnBattlefield(player2, "Oakenform");
        harness.assertInGraveyard(player2, "Oakenform");
    }

    @Test
    @DisplayName("Green noncombat damage is prevented while a white Aura remains attached")
    void greenAbilityDamageIsPrevented() {
        Permanent outlander = addCreatureReady(player1, new ZombieOutlander());
        Permanent bodyguard = addCreatureReady(player1, new RhoxBodyguard());
        addCreatureReady(player2, new ScattershotArcher());
        harness.setHand(player1, List.of(new AshasFavor(), new AshasFavor()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castEnchantment(player1, 0, outlander.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, bodyguard.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(outlander.getMarkedDamage()).isZero();
        assertThat(bodyguard.getMarkedDamage()).isEqualTo(1);
        assertThat(countPermanents(player1, "Asha's Favor")).isEqualTo(2);
    }

    @Test
    @DisplayName("Green mana in a white creature's ability cost does not prevent targeting")
    void whiteSourceWithGreenActivationCostCanTarget() {
        addCreatureReady(player1, new NacatlHuntPride());
        Permanent outlander = addCreatureReady(player2, new ZombieOutlander());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, outlander.getId());
        harness.passBothPriorities();

        assertThat(outlander.isMustBlockThisTurnIfAble()).isTrue();
    }

    @Test
    @DisplayName("A green creature's ability cannot target an otherwise eligible Outlander")
    void greenSourceAbilityCannotTarget() {
        addCreatureReady(player1, new BeaconBehemoth());
        Permanent outlander = addCreatureReady(player2, new ZombieOutlander());
        outlander.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, outlander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }
}

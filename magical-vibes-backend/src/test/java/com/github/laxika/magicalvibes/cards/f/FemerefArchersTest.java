package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SkyhunterSkirmisher;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FemerefArchersTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Femeref Archers has correct card properties")
    void hasCorrectProperties() {
        FemerefArchers card = new FemerefArchers();

        assertThat(card.getName()).isEqualTo("Femeref Archers");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.ARCHER);
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst()).isInstanceOf(DealDamageToTargetCreatureEffect.class);
        DealDamageToTargetCreatureEffect effect =
                (DealDamageToTargetCreatureEffect) card.getActivatedAbilities().getFirst().getEffects().getFirst();
        assertThat(effect.damage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Activating ability targeting attacking flying creature puts ability on stack")
    void activatingAbilityPutsOnStack() {
        Permanent archersPerm = addArchersReady(player1);
        Permanent attacker = addAttackingFlyingCreature(player2);

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(archersPerm.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Femeref Archers");
        assertThat(entry.getTargetPermanentId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Ability deals 4 damage to attacking flying creature and destroys it")
    void abilityDealsFourAndKillsTarget() {
        addArchersReady(player1);
        Permanent attacker = addAttackingFlyingCreature(player2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Femeref Archers"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Femeref Archers"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(attacker.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Skyhunter Skirmisher"));
    }

    @Test
    @DisplayName("Cannot target attacking creature without flying")
    void cannotTargetAttackingCreatureWithoutFlying() {
        addArchersReady(player1);
        Permanent attacker = addAttackingGroundCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature with flying");
    }

    @Test
    @DisplayName("Cannot target flying creature that is not attacking")
    void cannotTargetFlyingCreatureThatIsNotAttacking() {
        addArchersReady(player1);
        Permanent flyer = new Permanent(new SkyhunterSkirmisher());
        flyer.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(flyer);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature with flying");
    }

    @Test
    @DisplayName("Cannot activate ability while Femeref Archers has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent archers = new Permanent(new FemerefArchers());
        gd.playerBattlefields.get(player1.getId()).add(archers);
        Permanent attacker = addAttackingFlyingCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    private Permanent addArchersReady(Player player) {
        FemerefArchers card = new FemerefArchers();
        Permanent archers = new Permanent(card);
        archers.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(archers);
        return archers;
    }

    private Permanent addAttackingFlyingCreature(Player player) {
        Permanent flyer = new Permanent(new SkyhunterSkirmisher());
        flyer.setSummoningSick(false);
        flyer.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(flyer);
        return flyer;
    }

    private Permanent addAttackingGroundCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        creature.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}

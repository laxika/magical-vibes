package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.m.MindControl;
import com.github.laxika.magicalvibes.cards.w.Weakness;
import com.github.laxika.magicalvibes.cards.w.WarpathGhoul;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreatSableStag.class, Cancel.class, GrizzlyBears.class, Shock.class, GiantGrowth.class,
        MindControl.class, Weakness.class, WarpathGhoul.class})
class GreatSableStagTest extends BaseCardTest {

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
    @DisplayName("Great Sable Stag cannot be countered by Cancel")
    void cannotBeCounteredByCancel() {
        GreatSableStag stag = new GreatSableStag();
        harness.setHand(player1, List.of(stag));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, stag.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Great Sable Stag");
        harness.assertNotInGraveyard(player1, "Great Sable Stag");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Cannot be targeted by blue instant")
    void cannotBeTargetedByBlueInstant() {
        Permanent stag = addStagReady(player1);

        // Add valid target so spell is playable
        addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(createTargetedInstant("Blue Zap", CardColor.BLUE, "{U}")));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, stag.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Cannot be targeted by black instant")
    void cannotBeTargetedByBlackInstant() {
        Permanent stag = addStagReady(player1);

        // Add valid target so spell is playable
        addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(createTargetedInstant("Black Zap", CardColor.BLACK, "{B}")));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, stag.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be targeted by red instant")
    void canBeTargetedByRedInstant() {
        Permanent stag = addStagReady(player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, stag.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("Can be targeted by green instant")
    void canBeTargetedByGreenInstant() {
        Permanent stag = addStagReady(player1);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, stag.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Giant Growth");
    }

    @Test
    @DisplayName("Blue creature cannot block Great Sable Stag")
    void blueCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new GreatSableStag());
        attacker.setAttacking(true);


        Permanent blocker = addCreatureReady(player2, createCreature("Blue Flyer", 2, 2, CardColor.BLUE));


        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Black creature cannot block Great Sable Stag")
    void blackCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new GreatSableStag());
        attacker.setAttacking(true);


        Permanent blocker = addCreatureReady(player2, createCreature("Black Shade", 2, 2, CardColor.BLACK));


        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Red creature can block Great Sable Stag")
    void redCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new GreatSableStag());
        attacker.setAttacking(true);


        Permanent blocker = addCreatureReady(player2, createCreature("Red Goblin", 2, 2, CardColor.RED));


        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Great Sable Stag takes no combat damage from blue creature")
    void takesNoDamageFromBlueCreature() {
        Permanent attacker = addCreatureReady(player1, createCreature("Blue Giant", 4, 4, CardColor.BLUE));
        attacker.setAttacking(true);


        Permanent blocker = addCreatureReady(player2, new GreatSableStag());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);


        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Stag deals 3 to Blue Giant (4/4 survives), Blue Giant's 4 damage is prevented by protection
        harness.assertOnBattlefield(player2, "Great Sable Stag");
        harness.assertOnBattlefield(player1, "Blue Giant");
    }

    @Test
    @DisplayName("Great Sable Stag takes normal combat damage from red creature")
    void takesNormalDamageFromRedCreature() {
        Permanent attacker = addCreatureReady(player1, createCreature("Red Giant", 4, 4, CardColor.RED));
        attacker.setAttacking(true);


        Permanent blocker = addCreatureReady(player2, new GreatSableStag());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);


        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Red Giant deals 4 to Stag (4 >= 3 toughness, dies), Stag deals 3 to Red Giant (3 < 4, survives)
        harness.assertNotOnBattlefield(player2, "Great Sable Stag");
        harness.assertInGraveyard(player2, "Great Sable Stag");
        harness.assertOnBattlefield(player1, "Red Giant");
    }

    @Test
    @DisplayName("Blue Aura already attached to Stag goes to the graveyard")
    void removesAttachedBlueAura() {
        Permanent stag = addStagReady(player1);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new MindControl());
        aura.setAttachedTo(stag.getId());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Great Sable Stag");
        harness.assertNotOnBattlefield(player2, "Mind Control");
        harness.assertInGraveyard(player2, "Mind Control");
    }

    @Test
    @DisplayName("Black Aura already attached to Stag goes to the graveyard")
    void removesAttachedBlackAura() {
        Permanent stag = addStagReady(player1);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Weakness());
        aura.setAttachedTo(stag.getId());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Great Sable Stag");
        harness.assertNotOnBattlefield(player2, "Weakness");
        harness.assertInGraveyard(player2, "Weakness");
    }

    @Test
    @DisplayName("Stag can block a black creature and prevents its combat damage")
    void takesNoDamageFromBlackCreature() {
        Permanent attacker = addCreatureReady(player1, new WarpathGhoul());
        attacker.setAttacking(true);
        addStagReady(player2);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Great Sable Stag");
        harness.assertInGraveyard(player1, "Warpath Ghoul");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private Permanent addStagReady(Player player) {
        return addCreatureReady(player, new GreatSableStag());
    }
}

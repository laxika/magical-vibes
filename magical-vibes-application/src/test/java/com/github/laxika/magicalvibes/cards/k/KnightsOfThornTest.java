package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.m.MarshGoblins;
import com.github.laxika.magicalvibes.cards.s.Scarecrow;
import com.github.laxika.magicalvibes.cards.s.ScavengerFolk;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightsOfThorn.class, MarshGoblins.class, Scarecrow.class, ScavengerFolk.class})
class KnightsOfThornTest extends BaseCardTest {

    @Test
    @DisplayName("Red creature cannot block Knights of Thorn")
    void redCreatureCannotBlock() {
        Permanent knight = addCreatureReady(player1, new KnightsOfThorn());
        knight.setAttacking(true);
        addCreatureReady(player2, createCreature("Goblin", 2, 2, CardColor.RED));

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Knights of Thorn cannot be targeted by a red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent knight = addCreatureReady(player2, new KnightsOfThorn());
        addCreatureReady(player2, createCreature("Bear", 2, 2, CardColor.GREEN));

        harness.setHand(player1, List.of(createTargetedInstant("Red Bolt", CardColor.RED, "{R}")));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, knight.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Knights of Thorn takes no combat damage from a red creature")
    void takesNoCombatDamageFromRedCreature() {
        Permanent attacker = addCreatureReady(player2, createCreature("Goblin", 3, 3, CardColor.RED));
        attacker.setAttacking(true);
        Permanent knight = addCreatureReady(player1, new KnightsOfThorn());
        knight.setBlocking(true);
        knight.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Knights of Thorn can band with one other attacking creature")
    void canBandWithOneOtherAttackingCreature() {
        Permanent knight = addCreatureReady(player1, new KnightsOfThorn());
        Permanent partner = addCreatureReady(player1, new ScavengerFolk());
        Permanent blocker = addCreatureReady(player2, new Scarecrow());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1)));

        assertThat(knight.getBandId()).isNotNull();
        assertThat(partner.getBandId()).isEqualTo(knight.getBandId());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.getBlockingTargetIds()).containsExactlyInAnyOrder(knight.getId(), partner.getId());
    }

    @Test
    @DisplayName("Protection from red stops a multicolored red creature from blocking")
    void multicoloredRedCreatureCannotBlock() {
        Permanent knight = addCreatureReady(player1, new KnightsOfThorn());
        knight.setAttacking(true);
        addCreatureReady(player2, new MarshGoblins());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

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
}

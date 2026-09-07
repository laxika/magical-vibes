package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.d.DAvenantArcher;
import com.github.laxika.magicalvibes.cards.d.DrainLife;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.k.KjeldoranRoyalGuard;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        BlackKnight.class,
        DAvenantArcher.class,
        DrainLife.class,
        GiantSpider.class,
        GrizzlyBears.class,
        HolyStrength.class,
        KjeldoranRoyalGuard.class,
        UnholyStrength.class,
        WhiteKnight.class
})
class BlackKnightTest extends BaseCardTest {

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
    @DisplayName("Casting Black Knight puts it on the stack")
    void castingPutsOnStack() {
        BlackKnight blackKnight = new BlackKnight();
        harness.castFromHand(player1, blackKnight, "{B}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BlackKnight.class);
    }
    @Test
    @DisplayName("Resolving puts Black Knight on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new BlackKnight(), "{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Black Knight");
    }
    @Test
    @DisplayName("First strike kills 2/2 blocker before it deals regular damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new BlackKnight());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // First strike kills Bears before it deals damage; Black Knight survives
        harness.assertOnBattlefield(player1, "Black Knight");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
    @Test
    @DisplayName("White creature cannot block Black Knight")
    void whiteCreatureCannotBlockBlackKnight() {
        Permanent attacker = addCreatureReady(player1, new BlackKnight());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new WhiteKnight());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature can block Black Knight")
    void greenCreatureCanBlockBlackKnight() {
        Permanent attacker = addCreatureReady(player1, new BlackKnight());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
    @Test
    @DisplayName("Black Knight takes no combat damage from white creature")
    void takesNoDamageFromWhiteCreature() {
        Permanent attacker = addCreatureReady(player1, new KjeldoranRoyalGuard());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new BlackKnight());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Black Knight has first strike: deals 2 to the Royal Guard (2/5 survives)
        // The Royal Guard's 2 regular damage to Black Knight is prevented (protection)
        // Both survive
        harness.assertOnBattlefield(player1, "Kjeldoran Royal Guard");
        harness.assertOnBattlefield(player2, "Black Knight");
    }

    @Test
    @DisplayName("Black Knight takes normal combat damage from green creature")
    void takesNormalDamageFromGreenCreature() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new BlackKnight());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Black Knight deals 2 first strike (2 < 4, Giant Spider survives)
        // Giant Spider deals 2 regular damage, and Black Knight dies (no protection from green)
        harness.assertOnBattlefield(player1, "Giant Spider");
        harness.assertNotOnBattlefield(player2, "Black Knight");
        harness.assertInGraveyard(player2, "Black Knight");
    }
    @Test
    @DisplayName("Cannot be targeted by white instant")
    void cannotBeTargetedByWhiteInstant() {
        Permanent knight = addCreatureReady(player2, new BlackKnight());

        // Add valid target so spell is playable
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(createTargetedInstant("White Removal", CardColor.WHITE, "{W}")));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Cannot be targeted by a white ability")
    void cannotBeTargetedByWhiteAbility() {
        Permanent knight = addCreatureReady(player1, new BlackKnight());
        knight.setAttacking(true);
        Permanent archer = addCreatureReady(player2, new DAvenantArcher());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");

        assertThat(archer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can be targeted by black spell")
    void canBeTargetedByBlackSpell() {
        Permanent knight = addCreatureReady(player1, new BlackKnight());
        DrainLife drainLife = new DrainLife();

        harness.setHand(player1, List.of(drainLife));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1, knight.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(DrainLife.class);
    }
    @Test
    @DisplayName("Cannot be enchanted by white aura")
    void cannotBeEnchantedByWhiteAura() {
        Permanent knight = addCreatureReady(player2, new BlackKnight());

        // Add valid target so aura is playable
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Can be enchanted by black aura")
    void canBeEnchantedByBlackAura() {
        Permanent knight = addCreatureReady(player1, new BlackKnight());

        harness.setHand(player1, List.of(new UnholyStrength()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, knight.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(UnholyStrength.class);
    }
}

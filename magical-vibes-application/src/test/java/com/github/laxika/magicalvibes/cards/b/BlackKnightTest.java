package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlFromBeyond;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.i.IronrootTreefolk;
import com.github.laxika.magicalvibes.cards.n.NorthernPaladin;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlackKnight.class, GrizzlyBears.class, WhiteKnight.class, NorthernPaladin.class,
        IronrootTreefolk.class, SwordsToPlowshares.class, HowlFromBeyond.class,
        HolyStrength.class, UnholyStrength.class})
class BlackKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Black Knight puts it on the stack")
    void castingPutsOnStack() {
        BlackKnight blackKnight = new BlackKnight();
        harness.castFromHand(player1, blackKnight, "{B}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(blackKnight);
    }

    @Test
    @DisplayName("Resolving puts Black Knight on the battlefield")
    void resolvingPutsOnBattlefield() {
        BlackKnight blackKnight = new BlackKnight();
        harness.castFromHand(player1, blackKnight, "{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == blackKnight);
    }

    @Test
    @DisplayName("First strike kills 2/2 blocker before it deals regular damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new BlackKnight());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == attacker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() == blocker.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card == blocker.getCard());
    }

    @Test
    @DisplayName("White creature cannot block Black Knight")
    void whiteCreatureCannotBlockBlackKnight() {
        addCreatureReady(player1, new BlackKnight());
        addCreatureReady(player2, new WhiteKnight());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature can block Black Knight")
    void greenCreatureCanBlockBlackKnight() {
        addCreatureReady(player1, new BlackKnight());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Black Knight takes no combat damage from white creature")
    void takesNoDamageFromWhiteCreature() {
        Permanent attacker = addCreatureReady(player1, new NorthernPaladin());
        Permanent blocker = addCreatureReady(player2, new BlackKnight());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == attacker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == blocker.getCard());
    }

    @Test
    @DisplayName("Black Knight takes normal combat damage from green creature")
    void takesNormalDamageFromGreenCreature() {
        Permanent attacker = addCreatureReady(player1, new IronrootTreefolk());
        Permanent blocker = addCreatureReady(player2, new BlackKnight());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        // Black Knight deals 2 first strike (2 < 3, green survives)
        // Green deals 3 regular damage (3 >= 2, Black Knight dies — no protection from green)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == attacker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() == blocker.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card == blocker.getCard());
    }

    @Test
    @DisplayName("Cannot be targeted by white instant")
    void cannotBeTargetedByWhiteInstant() {
        Permanent knight = addCreatureReady(player2, new BlackKnight());
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Can be targeted by black instant")
    void canBeTargetedByBlackInstant() {
        Permanent knight = addCreatureReady(player1, new BlackKnight());
        HowlFromBeyond howlFromBeyond = new HowlFromBeyond();

        harness.setHand(player1, List.of(howlFromBeyond));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, 0, knight.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(howlFromBeyond);
    }

    @Test
    @DisplayName("Cannot be enchanted by white aura")
    void cannotBeEnchantedByWhiteAura() {
        Permanent knight = addCreatureReady(player2, new BlackKnight());
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
        UnholyStrength unholyStrength = new UnholyStrength();

        harness.setHand(player1, List.of(unholyStrength));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, knight.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(unholyStrength);
    }
}

package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AkkiLavarunner;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TokTokVolcanoBorn.class, AkkiLavarunner.class, AvatarOfMight.class,
        GrizzlyBears.class, LightningBolt.class, RagingGoblin.class, Humble.class})
class TokTokVolcanoBornTest extends BaseCardTest {

    /** Puts a flipped Akki Lavarunner (i.e. Tok-Tok) onto {@code player}'s battlefield. */
    private Permanent addTokTok(Player player) {
        Permanent tokTok = addCreatureReady(player, new AkkiLavarunner());
        tokTok.setTransformed(true);
        tokTok.setCard(tokTok.getOriginalCard().getBackFaceCard());
        return tokTok;
    }

    @Test
    @DisplayName("A red spell deals 1 extra damage to a player")
    void redSpellDealsOneMoreToPlayer() {
        addTokTok(player1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("The bonus applies no matter who controls Tok-Tok")
    void bonusAppliesWhenOpponentControlsTokTok() {
        addTokTok(player2);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("A red creature's combat damage to a player gets the bonus")
    void redCombatDamageGetsBonus() {
        addTokTok(player1);
        addCreatureReady(player1, new RagingGoblin());

        declareAttackers(List.of(1));
        resolveCombat(player1);

        // Raging Goblin is a 1/1: 1 damage plus 1.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A nonred creature's combat damage to a player is unchanged")
    void nonRedCombatDamageIsUnchanged() {
        addTokTok(player1);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveCombat(player1);

        // Grizzly Bears is a green 2/2: no bonus.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage dealt to a permanent gets no bonus")
    void damageToPermanentIsUnaffected() {
        addTokTok(player1);
        Permanent avatar = addCreatureReady(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, avatar.getId());
        harness.passBothPriorities();

        assertThat(avatar.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Protection from red stops a red spell from targeting Tok-Tok")
    void protectionFromRedStopsRedSpell() {
        Permanent tokTok = addTokTok(player1);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, tokTok.getId()))
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from red prevents a red creature from blocking Tok-Tok")
    void redCreatureCannotBlockTokTok() {
        addTokTok(player1);
        addCreatureReady(player2, new RagingGoblin());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from red prevents combat damage from red creatures")
    void redCombatDamageIsPrevented() {
        Permanent tokTok = addTokTok(player1);
        addCreatureReady(player2, new RagingGoblin());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(tokTok.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The bonus also applies to damage dealt to Tok-Tok's controller")
    void redSpellDealsOneMoreToController() {
        addTokTok(player1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Tok-Tok's own combat damage receives its red-source bonus")
    void ownCombatDamageGetsBonus() {
        addTokTok(player1);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Losing all abilities disables the damage bonus")
    void losingAbilitiesDisablesDamageBonus() {
        Permanent tokTok = addTokTok(player1);
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, tokTok.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrownOfFlames;
import com.github.laxika.magicalvibes.cards.d.DauthiMarauder;
import com.github.laxika.magicalvibes.cards.f.Fireslinger;
import com.github.laxika.magicalvibes.cards.k.KnightOfDawn;
import com.github.laxika.magicalvibes.cards.l.LightningBlast;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoltariPriest.class, LightningBlast.class, Pacifism.class, CrownOfFlames.class,
        Fireslinger.class, SoltariFootSoldier.class, KnightOfDawn.class, SpontaneousCombustion.class,
        DauthiMarauder.class, SoltariGuerrillas.class})
class SoltariPriestTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color,
                                       boolean shadow) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        if (shadow) {
            card.setKeywords(Set.of(Keyword.SHADOW));
        }
        return card;
    }

    @Test
    @DisplayName("Cannot be targeted by a red spell")
    void cannotBeTargetedByRedSpell() {
        Permanent priest = addCreatureReady(player2, new SoltariPriest());

        harness.setHand(player1, List.of(new LightningBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, priest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Can be targeted by a white spell")
    void canBeTargetedByWhiteSpell() {
        Permanent priest = addCreatureReady(player1, new SoltariPriest());

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, priest.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Survives combat damage from a red creature")
    void survivesDamageFromRedCreature() {
        addCreatureReady(player1, createCreature("Red Shadow", 3, 3, CardColor.RED, true));
        Permanent priest = addCreatureReady(player2, new SoltariPriest());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(priest.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(priest);
    }

    @Test
    @DisplayName("Survives noncombat damage from a red source")
    void survivesNoncombatDamageFromRedSource() {
        Permanent priest = addCreatureReady(player2, new SoltariPriest());
        addCreatureReady(player2, new KnightOfDawn());
        Permanent sacrifice = addCreatureReady(player1, new Fireslinger());

        harness.setHand(player1, List.of(new SpontaneousCombustion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(priest.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Soltari Priest");
        harness.assertNotOnBattlefield(player2, "Knight of Dawn");
        harness.assertInGraveyard(player2, "Knight of Dawn");
    }

    @Test
    @DisplayName("Cannot be blocked by a red creature")
    void cannotBeBlockedByRedCreature() {
        addCreatureReady(player1, new SoltariPriest());
        addCreatureReady(player2, createCreature("Red Shadow", 3, 3, CardColor.RED, true));

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Cannot be targeted by a red ability")
    void cannotBeTargetedByRedAbility() {
        Permanent priest = addCreatureReady(player2, new SoltariPriest());
        addCreatureReady(player1, new Fireslinger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, priest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Cannot be enchanted by a red Aura")
    void cannotBeEnchantedByRedAura() {
        Permanent priest = addCreatureReady(player2, new SoltariPriest());

        harness.setHand(player1, List.of(new CrownOfFlames()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, priest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Cannot block a creature without shadow")
    void cannotBlockCreatureWithoutShadow() {
        addCreatureReady(player1, new KnightOfDawn());
        addCreatureReady(player2, new SoltariPriest());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("Can block a creature with shadow")
    void canBlockCreatureWithShadow() {
        addCreatureReady(player1, new SoltariFootSoldier());
        Permanent priest = addCreatureReady(player2, new SoltariPriest());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(priest.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can be blocked by a creature with shadow")
    void canBeBlockedByCreatureWithShadow() {
        addCreatureReady(player1, new SoltariPriest());
        Permanent blocker = addCreatureReady(player2, new SoltariFootSoldier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot be blocked by a nonred creature without shadow")
    void cannotBeBlockedByCreatureWithoutShadow() {
        addCreatureReady(player1, new SoltariPriest());
        addCreatureReady(player2, new KnightOfDawn());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("Protection does not prevent combat damage from a black shadow creature")
    void diesToCombatDamageFromNonredShadowCreature() {
        addCreatureReady(player1, new DauthiMarauder());
        addCreatureReady(player2, new SoltariPriest());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Soltari Priest");
        harness.assertInGraveyard(player2, "Soltari Priest");
        harness.assertInGraveyard(player1, "Dauthi Marauder");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot be blocked by a red and white shadow creature")
    void cannotBeBlockedByMulticoloredRedCreature() {
        addCreatureReady(player1, new SoltariPriest());
        addCreatureReady(player2, new SoltariGuerrillas());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Can block a red and white shadow creature and prevent all its damage")
    void preventsCombatDamageFromMulticoloredRedCreature() {
        addCreatureReady(player1, new SoltariGuerrillas());
        Permanent priest = addCreatureReady(player2, new SoltariPriest());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(priest.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Soltari Priest");
        harness.assertInGraveyard(player1, "Soltari Guerrillas");
        harness.assertLife(player2, 20);
    }
}

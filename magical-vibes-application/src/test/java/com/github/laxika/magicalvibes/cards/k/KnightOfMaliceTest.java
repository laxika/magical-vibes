package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SamiteHealer;
import com.github.laxika.magicalvibes.cards.s.SelesnyaCharm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightOfMalice.class, BenalishKnight.class, GrizzlyBears.class, Pacifism.class,
        Shock.class, SamiteHealer.class, IcyManipulator.class, SelesnyaCharm.class})
class KnightOfMaliceTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's white spells cannot target Knight of Malice")
    void opponentWhiteSpellsCannotTarget() {
        harness.addToBattlefield(player2, new KnightOfMalice());

        // Add valid target so spell is playable
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                harness.getPermanentId(player2, "Knight of Malice")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("has hexproof from white");
    }

    @Test
    @DisplayName("Controller's own white spells can target Knight of Malice")
    void controllerWhiteSpellsCanTarget() {
        harness.addToBattlefield(player1, new KnightOfMalice());

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0,
                harness.getPermanentId(player1, "Knight of Malice"));

        GameData gd = harness.getGameData();
        assertThat(gd.stack)
                .anyMatch(se -> se.getCard().getName().equals("Pacifism"));
    }

    @Test
    @DisplayName("Non-white spells from opponents can target Knight of Malice")
    void nonWhiteSpellsCanTarget() {
        harness.addToBattlefield(player2, new KnightOfMalice());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Knight of Malice"));

        GameData gd = harness.getGameData();
        assertThat(gd.stack)
                .anyMatch(se -> se.getCard().getName().equals("Shock"));
    }

    @Test
    @DisplayName("Gets +1/+0 when any player controls a white permanent")
    void boostsWhenWhitePermanentExists() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfMalice());
        harness.addToBattlefield(player2, new BenalishKnight());

        GameData gd = harness.getGameData();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3); // 2 base + 1 boost
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2); // no toughness boost
    }

    @Test
    @DisplayName("Gets +1/+0 when controller controls a white permanent")
    void boostsWhenControllerHasWhitePermanent() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfMalice());
        harness.addToBattlefield(player1, new BenalishKnight());

        GameData gd = harness.getGameData();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3); // 2 base + 1 boost
    }

    @Test
    @DisplayName("No boost when no white permanent exists")
    void noBoostWithoutWhitePermanent() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfMalice());
        harness.addToBattlefield(player2, new GrizzlyBears());

        GameData gd = harness.getGameData();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2); // base only
    }

    @Test
    @DisplayName("Boost is +1/+0 regardless of how many white permanents exist")
    void boostDoesNotStackWithMultipleWhitePermanents() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfMalice());
        harness.addToBattlefield(player1, new BenalishKnight());
        harness.addToBattlefield(player2, new BenalishKnight());

        GameData gd = harness.getGameData();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3); // 2 base + 1 (not stacking)
    }

    @Test
    @DisplayName("First strike kills a blocker before it can damage Knight of Malice")
    void firstStrikeWhenAttacking() {
        Permanent knight = addCreatureReady(player1, new KnightOfMalice());
        knight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Knight of Malice");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("First strike also works while Knight of Malice is blocking")
    void firstStrikeWhenBlocking() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent knight = addCreatureReady(player1, new KnightOfMalice());
        knight.setBlocking(true);
        knight.addBlockingTarget(0);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Knight of Malice");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The power bonus ends when the last white permanent leaves")
    void losesBonusWhenLastWhitePermanentLeaves() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfMalice());
        Permanent whitePermanent = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, whitePermanent.getId());

        harness.assertInGraveyard(player2, "Benalish Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    @DisplayName("A white noncreature permanent also enables the power bonus")
    void whiteAuraEnablesBonusOnlyAfterResolving() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfMalice());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, knight.getId());
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pacifism");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's abilities from white sources cannot target Knight of Malice")
    void opponentWhiteAbilitiesCannotTarget() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfMalice());
        addCreatureReady(player2, new SamiteHealer());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("has hexproof from white");
    }

    @Test
    @DisplayName("Controller's abilities from white sources can target Knight of Malice")
    void controllerWhiteAbilitiesCanTarget() {
        Permanent healer = addCreatureReady(player1, new SamiteHealer());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfMalice());

        harness.activateAbility(player1, 0, null, knight.getId());
        harness.passBothPriorities();

        assertThat(healer.isTapped()).isTrue();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, knight.getId());
        harness.assertOnBattlefield(player1, "Knight of Malice");
    }

    @Test
    @DisplayName("Opponent's colorless abilities can target Knight of Malice")
    void opponentColorlessAbilitiesCanTarget() {
        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new KnightOfMalice());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, knight.getId());
        harness.passBothPriorities();

        assertThat(knight.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's green-white spells are still white and cannot target Knight of Malice")
    void opponentMulticoloredWhiteSpellsCannotTarget() {
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new KnightOfMalice());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SelesnyaCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("has hexproof from white");
    }
}

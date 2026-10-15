package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CabarettiInitiate;
import com.github.laxika.magicalvibes.cards.c.CallInAProfessional;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelOfSuffering.class, GrizzlyBears.class, LightningBolt.class,
        CabarettiInitiate.class, CallInAProfessional.class, WitnessProtection.class})
class AngelOfSufferingTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents noncombat damage and mills twice that much")
    void preventsNoncombatDamageAndMillsTwiceThatMuch() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AngelOfSuffering());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castLightningBoltAtPlayer1();

        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Prevents damage even when the library has too few cards")
    void preventsDamageWithShortLibrary() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AngelOfSuffering());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castLightningBoltAtPlayer1();

        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Prevents combat damage and mills twice that much")
    void preventsCombatDamageAndMillsTwiceThatMuch() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new AngelOfSuffering());
        harness.setLibrary(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Still mills when damage prevention is disabled")
    void stillMillsWhenDamageCannotBePrevented() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AngelOfSuffering());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        gd.damageCantBePreventedThisTurn = true;
        castLightningBoltAtPlayer1();

        harness.assertLife(player1, 17);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Losing all abilities disables spell damage prevention and milling")
    void losingAbilitiesDisablesSpellDamagePrevention() {
        harness.setLife(player1, 20);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new AngelOfSuffering());
        harness.setLibrary(player1, List.of(new CabarettiInitiate(), new CabarettiInitiate()));
        harness.setHand(player1, List.of(new WitnessProtection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, angel.getId());
        harness.passBothPriorities();

        castLightningBoltAtPlayer1();

        harness.assertLife(player1, 17);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Losing all abilities disables combat damage prevention and milling")
    void losingAbilitiesDisablesCombatDamagePrevention() {
        harness.setLife(player2, 20);
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new AngelOfSuffering());
        harness.setLibrary(player2, List.of(new CabarettiInitiate(), new CabarettiInitiate()));
        harness.setHand(player1, List.of(new WitnessProtection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, angel.getId());
        harness.passBothPriorities();
        Permanent attacker = addCreatureReady(player1, new CabarettiInitiate());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 19);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Multiple Angels mill only once for preventable damage")
    void multipleAngelsDoNotMultiplyMillingForPreventableDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new AngelOfSuffering());
        harness.addToBattlefield(player2, new AngelOfSuffering());
        harness.setLibrary(player2, List.of(new CabarettiInitiate(), new CabarettiInitiate(),
                new CabarettiInitiate(), new CabarettiInitiate(), new CabarettiInitiate()));
        Permanent attacker = addCreatureReady(player1, new CabarettiInitiate());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Each Angel mills for unpreventable damage")
    void eachAngelMillsForUnpreventableDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AngelOfSuffering());
        harness.addToBattlefield(player1, new AngelOfSuffering());
        harness.setLibrary(player1, java.util.stream.IntStream.range(0, 13)
                .mapToObj(i -> new CabarettiInitiate()).toList());
        harness.setHand(player2, List.of(new CallInAProfessional()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(12);
    }

    @Test
    @DisplayName("An empty library does not stop prevention or cause a loss")
    void preventsDamageWithEmptyLibrary() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AngelOfSuffering());
        harness.setLibrary(player1, List.of());

        castLightningBoltAtPlayer1();

        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private void castLightningBoltAtPlayer1() {
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
    }
}

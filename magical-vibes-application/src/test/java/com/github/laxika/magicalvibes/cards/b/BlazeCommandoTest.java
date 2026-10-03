package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FieryConfluence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PunishTheEnemy;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.r.RiotControl;
import com.github.laxika.magicalvibes.cards.s.SearingBlood;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnBurn;
import com.github.laxika.magicalvibes.cards.w.WarleadersHelix;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlazeCommando.class, GrizzlyBears.class, Pyroclasm.class, Shock.class,
        FieryConfluence.class, PunishTheEnemy.class, RiotControl.class, SearingBlood.class,
        TurnBurn.class, WarleadersHelix.class, WindDrake.class})
class BlazeCommandoTest extends BaseCardTest {

    private long soldierTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Soldier".equals(p.getCard().getName()))
                .count();
    }

    @Test
    @DisplayName("An instant you control dealing damage creates two hasty Soldier tokens")
    void instantDamageCreatesTwoSoldiers() {
        harness.addToBattlefield(player1, new BlazeCommando());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(soldierTokens(player1)).isEqualTo(2);
        Permanent soldier = findPermanents(player1, "Soldier").getFirst();
        assertThat(soldier.getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("One spell damaging several creatures at once triggers only once")
    void simultaneousDamageTriggersOnce() {
        harness.addToBattlefield(player1, new BlazeCommando());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        resolveAllTriggers();

        assertThat(soldierTokens(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's damage spell does not trigger it")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new BlazeCommando());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(soldierTokens(player1)).isZero();
        assertThat(soldierTokens(player2)).isZero();
    }

    @Test
    @DisplayName("Combat damage from a creature does not trigger it")
    void combatDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new BlazeCommando());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(soldierTokens(player1)).isZero();
    }

    @Test
    @DisplayName("Three separate damage instructions in one spell create six Soldiers")
    void separateDamageEventsTriggerSeparately() {
        harness.addToBattlefield(player1, new BlazeCommando());
        harness.setHand(player1, List.of(new FieryConfluence()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castModalSorcery(player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 1, 1), List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        assertThat(soldierTokens(player1)).isEqualTo(6);
    }

    @Test
    @DisplayName("Damage from Searing Blood's delayed ability is not damage from a spell")
    void delayedAbilityDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new BlazeCommando());
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        harness.setHand(player1, List.of(new SearingBlood()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, drake.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Wind Drake");
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(soldierTokens(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("One damage instruction hitting a player and creature triggers once")
    void simultaneousPlayerAndCreatureDamageTriggersOnce() {
        harness.addToBattlefield(player1, new BlazeCommando());
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        harness.setHand(player1, List.of(new PunishTheEnemy()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId(), drake.getId()));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player2, "Wind Drake");
        assertThat(soldierTokens(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Fully prevented damage does not trigger the ability")
    void preventedDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new BlazeCommando());
        harness.setHand(player2, List.of(new RiotControl()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0);
        int lifeBeforeDamage = gd.getLife(player2.getId());

        harness.setHand(player1, List.of(new WarleadersHelix()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBeforeDamage);
        assertThat(soldierTokens(player1)).isZero();
    }

    @Test
    @DisplayName("Lethal spell damage to Blaze Commando still creates the specified tokens")
    void lethalDamageToWatcherStillCreatesTokens() {
        Permanent commando = harness.addToBattlefieldAndReturn(player1, new BlazeCommando());
        harness.setHand(player1, List.of(new WarleadersHelix()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, commando.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Blaze Commando");
        assertThat(soldierTokens(player1)).isEqualTo(2);
        assertThat(findPermanents(player1, "Soldier")).allSatisfy(soldier -> {
            assertThat(soldier.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(soldier.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
            assertThat(soldier.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
            assertThat(soldier.getCard().getPower()).isEqualTo(1);
            assertThat(soldier.getCard().getToughness()).isEqualTo(1);
            assertThat(soldier.getCard().getKeywords()).contains(Keyword.HASTE);
            assertThat(soldier.isTapped()).isFalse();
        });
    }

    @Test
    @DisplayName("Blaze Commando cannot trigger while Turn has removed its abilities")
    void removedAbilitiesDoNotTrigger() {
        Permanent commando = harness.addToBattlefieldAndReturn(player1, new BlazeCommando());
        harness.setHand(player1, List.of(new TurnBurn(), new WarleadersHelix()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, 0, commando.getId());
        harness.passBothPriorities();
        assertThat(commando.getEffectiveToughness()).isEqualTo(1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(soldierTokens(player1)).isZero();
    }
}

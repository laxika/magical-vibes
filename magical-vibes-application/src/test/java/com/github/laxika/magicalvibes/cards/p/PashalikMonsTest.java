package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CardColor;




@CardUsed({PashalikMons.class, GoblinPiker.class, GrizzlyBears.class, Shock.class,
        Pyroclasm.class, BoggartShenanigans.class})
class PashalikMonsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to any target when another Goblin you control dies")
    void goblinDeathDealsDamage() {
        harness.addToBattlefield(player1, new PashalikMons());
        Permanent piker = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        harness.setLife(player2, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, piker.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A non-Goblin creature death does not trigger Pashalik Mons")
    void nonGoblinDeathDoesNotDealDamage() {
        harness.addToBattlefield(player1, new PashalikMons());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Sacrificing a Goblin creates two red Goblin tokens")
    void sacrificeGoblinCreatesTwoTokens() {
        Permanent pashalik = harness.addToBattlefieldAndReturn(player1, new PashalikMons());
        Permanent piker = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, piker.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pashalik);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.GOBLIN)))
                .hasSize(2);
    }

    @Test
    @DisplayName("An opponent's Goblin dying does not trigger Pashalik Mons")
    void opposingGoblinDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new PashalikMons());
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinPiker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, goblin.getId());

        harness.assertInGraveyard(player2, "Goblin Piker");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Pashalik Mons can sacrifice itself and still create tokens")
    void sacrificingSelfCreatesTokensAndTriggersDamage() {
        Permanent pashalik = harness.addToBattlefieldAndReturn(player1, new PashalikMons());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, pashalik.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Pashalik Mons");
        harness.assertLife(player2, 19);
        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
    }

    @Test
    @DisplayName("Simultaneous deaths trigger once for Pashalik and each allied Goblin")
    void simultaneousGoblinDeathsEachTrigger() {
        harness.addToBattlefield(player1, new PashalikMons());
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, player2.getId());
        }
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A noncreature Goblin can pay the sacrifice cost without a death trigger")
    void canSacrificeKindredGoblinEnchantment() {
        harness.addToBattlefield(player1, new PashalikMons());
        Permanent shenanigans = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, shenanigans.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Boggart Shenanigans");
        harness.assertOnBattlefield(player1, "Pashalik Mons");
        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A Goblin token dying can deal damage to a creature")
    void tokenDeathCanTargetCreature() {
        harness.addToBattlefield(player1, new PashalikMons());
        Permanent piker = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, piker.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        Permanent token = findPermanents(player1, "Goblin").getFirst();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, token.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 19);
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}

@CardUsed({PashalikMons.class, RagingGoblin.class, GrizzlyBears.class, Shock.class})
class Mh1PashalikMonsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to any target when another Goblin you control dies")
    void goblinDeathTriggersDamage() {
        harness.addToBattlefield(player1, new PashalikMons());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setLife(player2, 20);

        killWithShock(goblin);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger when a non-Goblin you control dies")
    void nonGoblinDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new PashalikMons());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        killWithShock(creature);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals 1 damage when Pashalik Mons itself dies")
    void selfDeathTriggersDamage() {
        Permanent pashalik = harness.addToBattlefieldAndReturn(player1, new PashalikMons());
        harness.setLife(player2, 20);

        killWithShock(pashalik);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Pashalik Mons");
    }

    @Test
    @DisplayName("Sacrificing a Goblin creates two 1/1 red Goblin tokens")
    void sacrificingGoblinCreatesTwoTokens() {
        Permanent pashalik = harness.addToBattlefieldAndReturn(player1, new PashalikMons());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, battlefieldIndex(pashalik), 0, null, null);
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Goblin"))
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    private void killWithShock(Permanent target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}

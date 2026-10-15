package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlingGangLieutenant.class, RagingGoblin.class, GrizzlyBears.class, BoggartShenanigans.class})
class SlingGangLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates two 1/1 red Goblin tokens")
    void entersCreatesGoblinTokens() {
        harness.setHand(player1, List.of(new SlingGangLieutenant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Goblin");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Sacrificing a Goblin makes a target player lose 1 life and gains 1 life")
    void sacrificingGoblinDrainsTargetPlayer() {
        Permanent lieutenant = addCreatureReady(player1, new SlingGangLieutenant());
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, battlefieldIndex(lieutenant), 0, null, player2.getId());
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("The ability can sacrifice Sling-Gang Lieutenant itself")
    void canSacrificeItself() {
        addCreatureReady(player1, new SlingGangLieutenant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player1, "Sling-Gang Lieutenant");
        harness.assertInGraveyard(player1, "Sling-Gang Lieutenant");
    }

    @Test
    @DisplayName("The sacrifice cost only accepts Goblins")
    void sacrificeCostOnlyAcceptsGoblins() {
        Permanent lieutenant = addCreatureReady(player1, new SlingGangLieutenant());
        addCreatureReady(player1, new RagingGoblin());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(lieutenant), 0, null, player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("A noncreature Goblin permanent can pay the sacrifice cost")
    void canSacrificeNoncreatureGoblin() {
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new SlingGangLieutenant());
        Permanent shenanigans = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, battlefieldIndex(lieutenant), 0, null, player2.getId());
        harness.handlePermanentChosen(player1, shenanigans.getId());
        harness.assertInGraveyard(player1, "Boggart Shenanigans");
        harness.assertOnBattlefield(player1, "Sling-Gang Lieutenant");
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The controller can target themselves, losing and gaining one life")
    void canTargetController() {
        harness.addToBattlefield(player1, new SlingGangLieutenant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.assertInGraveyard(player1, "Sling-Gang Lieutenant");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Both generated Goblin tokens can be sacrificed immediately")
    void canSacrificeBothGeneratedTokens() {
        Permanent lieutenant = harness.enterBattlefieldAndReturn(player1, new SlingGangLieutenant());
        harness.passBothPriorities();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        List<Permanent> tokens = findPermanents(player1, "Goblin");
        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            harness.activateAbility(player1, battlefieldIndex(lieutenant), 0, null, player2.getId());
            harness.handlePermanentChosen(player1, token.getId());
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
        harness.assertOnBattlefield(player1, "Sling-Gang Lieutenant");
    }

    @Test
    @DisplayName("An opponent's Goblin cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsGoblin() {
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new SlingGangLieutenant());
        harness.addToBattlefield(player1, new RagingGoblin());
        Permanent opponentsGoblin = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        harness.activateAbility(player1, battlefieldIndex(lieutenant), 0, null, player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentsGoblin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.assertOnBattlefield(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("The enter trigger creates tokens even if the Lieutenant sacrifices itself first")
    void enterTriggerResolvesAfterSourceIsSacrificed() {
        Permanent lieutenant = harness.enterBattlefieldAndReturn(player1, new SlingGangLieutenant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, battlefieldIndex(lieutenant), 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Sling-Gang Lieutenant");
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Sling-Gang Lieutenant");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}

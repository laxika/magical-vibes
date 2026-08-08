package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VeilOfSecrecyTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gains shroud and can't be blocked")
    void grantsShroudAndUnblockable() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VeilOfSecrecy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.hasKeyword(Keyword.SHROUD)).isTrue();
        assertThat(bear.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Shroud and unblockable wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VeilOfSecrecy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.hasKeyword(Keyword.SHROUD)).isFalse();
        assertThat(bear.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VeilOfSecrecy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = player1.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell by returning a blue creature, staying in hand")
    void splicesOntoArcaneSpell() {
        Card arcaneShock = new Shock().createRuntimeCopy();
        arcaneShock.setSubtypes(List.of(CardSubtype.ARCANE));
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(arcaneShock, new VeilOfSecrecy()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithSplice(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"), List.of(1),
                List.of(harness.getPermanentId(player1, "Fugitive Wizard")));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fugitive Wizard"));
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Veil of Secrecy", "Fugitive Wizard");
    }

    @Test
    @DisplayName("Cannot pay the splice cost by returning a creature that is not blue")
    void cannotReturnNonBlueCreatureForSplice() {
        Card arcaneShock = new Shock().createRuntimeCopy();
        arcaneShock.setSubtypes(List.of(CardSubtype.ARCANE));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(arcaneShock, new VeilOfSecrecy()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID ownBearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID enemyBearId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, enemyBearId, List.of(1), List.of(ownBearId)))
                .isInstanceOf(IllegalStateException.class);
    }
}

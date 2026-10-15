package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScarbladesMalice.class, GrizzlyBears.class, GoForTheThroat.class})
class ScarbladesMaliceTest extends BaseCardTest {

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Grants a creature you control deathtouch and lifelink until end of turn")
    void grantsDeathtouchAndLifelink() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScarbladesMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Creates a 2/2 black and green Elf when the targeted creature dies this turn")
    void createsElfWhenTargetedCreatureDies() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScarbladesMalice(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.castInstant(player1, 0, targetId);
        resolveStack();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Elf");
    }

    @Test
    @DisplayName("Does not create an Elf if the targeted creature survives")
    void doesNotCreateElfIfTargetSurvives() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScarbladesMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Elf");
    }

    @Test
    @DisplayName("Keyword grants wear off at end of turn")
    void keywordGrantsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScarbladesMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScarbladesMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A target that dies before resolution does not create an Elf")
    void targetDiesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScarbladesMalice(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, bears.getId());
        harness.castInstant(player1, 0, bears.getId());
        resolveStack();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Elf");
    }

    @Test
    @DisplayName("The delayed death trigger expires when the turn ends")
    void deathOnNextTurnDoesNotCreateElf() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScarbladesMalice(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        resolveStack();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Elf");
    }

    @Test
    @DisplayName("Two resolved copies create two Elves when the target dies")
    void multipleCopiesCreateSeparateTokens() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScarbladesMalice(), new ScarbladesMalice(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.castAndResolveInstant(player1, 0, bears.getId());
        resolveStack();

        assertThat(findPermanents(player1, "Elf")).hasSize(2).allSatisfy(elf -> {
            assertThat(elf.getCard().isToken()).isTrue();
            assertThat(elf.getCard().getPower()).isEqualTo(2);
            assertThat(elf.getCard().getToughness()).isEqualTo(2);
            assertThat(elf.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
            assertThat(elf.getCard().getSubtypes()).containsExactly(CardSubtype.ELF);
            assertThat(elf.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
            assertThat(elf.hasKeyword(Keyword.LIFELINK)).isFalse();
        });
        harness.assertNotOnBattlefield(player2, "Elf");
    }
}

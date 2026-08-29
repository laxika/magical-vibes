package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExtricatorOfSinTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may sacrifice another permanent to create an Eldrazi Horror")
    void etbSacrificeCreatesToken() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareMainPhase();
        harness.setHand(player1, List.of(new ExtricatorOfSin()));
        addManaForFrontFace();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(sacrifice.getId()));
        assertThat(findToken()).satisfies(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(3);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.HORROR);
        });
    }

    @Test
    @DisplayName("Declining the ETB sacrifice creates no token")
    void decliningEtbSacrificeCreatesNoToken() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareMainPhase();
        harness.setHand(player1, List.of(new ExtricatorOfSin()));
        addManaForFrontFace();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(sacrifice.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Delirium transforms Extricator of Sin at upkeep")
    void deliriumTransformsAtUpkeep() {
        Permanent extricator = addFrontFace(player1);
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(extricator.isTransformed()).isTrue();
        assertThat(extricator.getCard()).isInstanceOf(ExtricatorOfFlesh.class);
    }

    @Test
    @DisplayName("Without delirium, Extricator of Sin stays on its front face")
    void withoutDeliriumDoesNotTransform() {
        Permanent extricator = addFrontFace(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Plains(), new Shock()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(extricator.isTransformed()).isFalse();
        assertThat(extricator.getCard()).isInstanceOf(ExtricatorOfSin.class);
    }

    @Test
    @DisplayName("Extricator of Flesh's activated ability sacrifices a non-Eldrazi creature")
    void backFaceAbilityCreatesVigilantToken() {
        Permanent extricator = addBackFace(player1);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, extricator, Keyword.VIGILANCE)).isTrue();
        assertThat(findToken()).satisfies(token ->
                assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue());
    }

    private Permanent addFrontFace(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ExtricatorOfSin());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addBackFace(Player player) {
        ExtricatorOfSin card = new ExtricatorOfSin();
        Permanent permanent = new Permanent(card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent findToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Eldrazi Horror"))
                .findFirst()
                .orElseThrow();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private void addManaForFrontFace() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}

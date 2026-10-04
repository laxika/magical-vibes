package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WretchedGryff;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExtricatorOfSin.class, ExtricatorOfFlesh.class, GrizzlyBears.class,
        Millstone.class, Plains.class, Shock.class, WretchedGryff.class})
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
        harness.passUntil(TurnStep.UPKEEP);
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
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(extricator.isTransformed()).isFalse();
        assertThat(extricator.getCard()).isInstanceOf(ExtricatorOfSin.class);
    }

    @Test
    @DisplayName("Extricator of Flesh's activated ability sacrifices a non-Eldrazi creature")
    void backFaceAbilityCreatesVigilantToken() {
        Permanent extricator = addBackFace(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, extricator, Keyword.VIGILANCE)).isTrue();
        assertThat(findToken()).satisfies(token ->
                assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue());
    }

    @Test
    @DisplayName("ETB can sacrifice a land and creates its token during the same resolution")
    void etbCanSacrificeLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        prepareMainPhase();
        harness.setHand(player1, List.of(new ExtricatorOfSin()));
        addManaForFrontFace();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, land.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(findToken()).isNotNull();
    }

    @Test
    @DisplayName("Delirium must still be present when the upkeep trigger resolves")
    void losingDeliriumBeforeResolutionPreventsTransform() {
        Permanent extricator = addFrontFace(player1);
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.UPKEEP);
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Plains(), new Shock()));
        harness.passBothPriorities();

        assertThat(extricator.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Gaining delirium after upkeep begins does not create a transform trigger")
    void gainingDeliriumAfterUpkeepDoesNotTransform() {
        Permanent extricator = addFrontFace(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Plains(), new Shock()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.UPKEEP);
        assertThat(gd.stack).isEmpty();

        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()));
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);

        assertThat(extricator.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Delirium does not transform the creature during an opponent's upkeep")
    void opponentsUpkeepDoesNotTransform() {
        Permanent extricator = addFrontFace(player1);
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);

        assertThat(extricator.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Back face pays its tap and sacrifice costs before creating a token")
    void backFacePaysCostsBeforeResolution() {
        Permanent extricator = addBackFace(player1);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(extricator.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        harness.passBothPriorities();
        assertThat(findToken()).isNotNull();
    }

    @Test
    @DisplayName("Back face cannot sacrifice itself, a land, or an opponent's creature")
    void backFaceRequiresOwnNonEldraziCreature() {
        Permanent extricator = addBackFace(player1);
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(extricator.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vigilance applies only to Eldrazi controlled by Extricator of Flesh's controller")
    void vigilanceIsRestrictedToOwnEldrazi() {
        Permanent extricator = addBackFace(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownEldrazi = harness.addToBattlefieldAndReturn(player1, new WretchedGryff());
        Permanent opposingEldrazi = harness.addToBattlefieldAndReturn(player2, new WretchedGryff());

        assertThat(gqs.hasKeyword(gd, extricator, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownEldrazi, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingEldrazi, Keyword.VIGILANCE)).isFalse();
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

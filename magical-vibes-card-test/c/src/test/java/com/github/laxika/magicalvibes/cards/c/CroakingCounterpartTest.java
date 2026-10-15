package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BirdAdmirer;
import com.github.laxika.magicalvibes.cards.d.Defenestrate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HazeFrog;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.NoviceOccultist;
import com.github.laxika.magicalvibes.cards.w.WingShredder;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CroakingCounterpart.class, HazeFrog.class, HillGiant.class, BirdAdmirer.class,
        WingShredder.class, Defenestrate.class, Forest.class, NoviceOccultist.class})
class CroakingCounterpartTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a green 1/1 Frog token copy with only the Frog creature type")
    void createsGreenOneOneFrogTokenCopy() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new CroakingCounterpart()));
        addCroakingCounterpartMana();

        UUID targetId = harness.getPermanentId(player1, "Hill Giant");
        harness.castAndResolveSorcery(player1, 0, targetId);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.FROG);
    }

    @Test
    @DisplayName("Cannot target a Frog creature")
    void cannotTargetFrogCreature() {
        harness.addToBattlefield(player1, new HazeFrog());
        harness.setHand(player1, List.of(new CroakingCounterpart()));
        addCroakingCounterpartMana();

        UUID targetId = harness.getPermanentId(player1, "Haze Frog");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback creates the Frog token and exiles the spell")
    void flashbackCreatesTokenAndExilesSpell() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new CroakingCounterpart()));
        addCroakingCounterpartFlashbackMana();

        UUID targetId = harness.getPermanentId(player1, "Hill Giant");
        harness.castAndResolveFlashback(player1, 0, targetId);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Croaking Counterpart"));
    }

    @Test
    void copiesOpponentsCreatureUnderSpellControllersControl() {
        harness.addToBattlefield(player2, new NoviceOccultist());
        harness.setHand(player1, List.of(new CroakingCounterpart()));
        addCroakingCounterpartMana();

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Novice Occultist"));

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getName()).isEqualTo("Novice Occultist");
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.FROG);
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Croaking Counterpart");
    }

    @Test
    void frogCopyRetainsDeathAbility() {
        harness.addToBattlefield(player2, new NoviceOccultist());
        harness.setHand(player1, List.of(new CroakingCounterpart()));
        addCroakingCounterpartMana();
        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Novice Occultist"));

        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new Defenestrate()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, token.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Novice Occultist");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new CroakingCounterpart()));
        addCroakingCounterpartMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, harness.getPermanentId(player1, "Forest")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetFrogTokenCreatedByAnEarlierCopy() {
        harness.addToBattlefield(player2, new NoviceOccultist());
        harness.setHand(player1, List.of(new CroakingCounterpart(), new CroakingCounterpart()));
        addCroakingCounterpartMana();
        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Novice Occultist"));
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        addCroakingCounterpartMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, token.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flashbackWithRemovedTargetCreatesNoTokenAndStillExilesSpell() {
        harness.addToBattlefield(player2, new NoviceOccultist());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(new CroakingCounterpart()));
        harness.setHand(player1, List.of(new Defenestrate()));
        addCroakingCounterpartFlashbackMana();
        UUID targetId = harness.getPermanentId(player2, "Novice Occultist");
        harness.castFlashback(player1, 0, targetId);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Croaking Counterpart");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Croaking Counterpart"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyingDayboundCreatureCreatesTokenThatTransformsAtNight() {
        gd.dayNight = DayNight.DAY;
        Permanent admirer = harness.enterBattlefieldAndReturn(player2, new BirdAdmirer());
        harness.setHand(player1, List.of(new CroakingCounterpart()));
        addCroakingCounterpartMana();
        harness.castAndResolveSorcery(player1, 0, admirer.getId());
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(token.isTransformed()).isTrue();
        assertThat(token.getCard().getName()).isEqualTo("Wing Shredder");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.FROG);
    }

    @Test
    void copyingNightboundCreatureCreatesTokenThatReturnsToFrontFaceAtDay() {
        gd.dayNight = DayNight.NIGHT;
        Permanent admirer = harness.enterBattlefieldAndReturn(player2, new BirdAdmirer());
        harness.setHand(player1, List.of(new CroakingCounterpart()));
        addCroakingCounterpartMana();
        harness.castAndResolveSorcery(player1, 0, admirer.getId());
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(token.getCard().getName()).isEqualTo("Wing Shredder");
        assertThat(token.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        gd.previousTurnActivePlayerId = player2.getId();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(token.isTransformed()).isFalse();
        assertThat(token.getCard().getName()).isEqualTo("Bird Admirer");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.FROG);
    }

    private void addCroakingCounterpartMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addCroakingCounterpartFlashbackMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}

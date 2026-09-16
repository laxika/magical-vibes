package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FirecatBlitz;
import com.github.laxika.magicalvibes.cards.h.HaplessResearcher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Spelljack.class, SuntailHawk.class, FirecatBlitz.class, HaplessResearcher.class})
class SpelljackTest extends BaseCardTest {

    @Test
    void countersAndExilesTargetSpellWithFreeCastPermissionForItsController() {
        SuntailHawk hawk = new SuntailHawk();
        Spelljack spelljack = new Spelljack();
        harness.setHand(player1, List.of(hawk));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(spelljack));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hawk.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(hawk.getId()));
        assertThat(gd.exilePlayPermissions.get(hawk.getId())).isEqualTo(player2.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(hawk.getId());
        harness.assertNotInGraveyard(player1, "Suntail Hawk");
        harness.assertNotOnBattlefield(player1, "Suntail Hawk");
    }

    @Test
    void controllerCanCastCounteredSpellFromExileWithoutMana() {
        SuntailHawk hawk = new SuntailHawk();
        Spelljack spelljack = new Spelljack();
        harness.setHand(player1, List.of(hawk));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(spelljack));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hawk.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player2, hawk.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Suntail Hawk");
        assertThat(gd.findExiledCard(hawk.getId())).isNull();
    }

    @Test
    void cannotTargetPermanent() {
        var permanent = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setHand(player2, List.of(new Spelljack()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetActivatedAbility() {
        harness.addToBattlefieldAndReturn(player1, new HaplessResearcher());
        harness.setHand(player2, List.of(new Spelljack()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        var ability = gd.stack.getLast();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, ability.getTargetableId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void castsCounteredXSpellWithXEqualToZero() {
        FirecatBlitz firecatBlitz = new FirecatBlitz();
        Spelljack spelljack = new Spelljack();
        harness.setHand(player1, List.of(firecatBlitz));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setHand(player2, List.of(spelljack));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, firecatBlitz.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player2, firecatBlitz.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Firecat Blitz");
        assertThat(gd.findExiledCard(firecatBlitz.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Elemental Cat"));
    }
}

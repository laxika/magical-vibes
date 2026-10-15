package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RootriderFaun;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SwiftSpiral;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwiningTwins.class, SwiftSpiral.class, RootriderFaun.class, Island.class})
class TwiningTwinsTest extends BaseCardTest {

    @Test
    void adventureExilesNontokenCreatureAndReturnsItAtNextEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RootriderFaun());
        TwiningTwins card = new TwiningTwins();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Rootrider Faun");
        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());

        harness.passUntil(TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player2, "Rootrider Faun");
        harness.passBothPriorities();

        Permanent returned = findPermanent(player2, "Rootrider Faun");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(gd.findExiledCard(target.getCard().getId())).isNull();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCannotTargetTokenCreature() {
        RootriderFaun token = new RootriderFaun();
        token.setToken(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, token);
        harness.setHand(player1, List.of(new TwiningTwins()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adventureCannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new TwiningTwins()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RootriderFaun());
        TwiningTwins card = new TwiningTwins();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Twining Twins");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void stolenCreatureReturnsToItsOwnerRatherThanItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RootriderFaun());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.setHand(player1, List.of(new TwiningTwins()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Rootrider Faun");

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Rootrider Faun");
        harness.assertNotOnBattlefield(player1, "Rootrider Faun");
    }

    @Test
    void adventureCastDuringEndStepReturnsCreatureAtFollowingTurnsEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RootriderFaun());
        harness.setLibrary(player2, List.of(new RootriderFaun(), new RootriderFaun()));
        harness.setHand(player1, List.of(new TwiningTwins()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.castAdventure(player1, 0, target.getId());
            harness.passBothPriorities();
            harness.assertNotOnBattlefield(player2, "Rootrider Faun");
            assertThat(gd.stack).isEmpty();
        });

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.assertNotOnBattlefield(player2, "Rootrider Faun");
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player2, "Rootrider Faun");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Rootrider Faun");
    }

    @Test
    void adventureWithTargetExiledInResponseGoesToGraveyardWithoutCastPermission() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RootriderFaun());
        TwiningTwins first = new TwiningTwins();
        TwiningTwins response = new TwiningTwins();
        harness.setHand(player1, List.of(first));
        harness.setHand(player2, List.of(response));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAdventure(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Twining Twins");
        assertThat(gd.findExiledCard(first.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(first.getId());
        assertThat(gd.findExiledCard(response.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(response.getId())).isEqualTo(player2.getId());
    }
}

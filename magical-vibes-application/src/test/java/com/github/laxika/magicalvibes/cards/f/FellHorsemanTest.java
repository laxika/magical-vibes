package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DeathlyRide;
import com.github.laxika.magicalvibes.cards.d.Dreadbore;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FellHorseman.class, DeathlyRide.class, Dreadbore.class, GrizzlyBears.class, Plains.class})
class FellHorsemanTest extends BaseCardTest {

    @Test
    void adventureReturnsTargetCreatureCardToHandAndExilesTheCard() {
        GrizzlyBears target = new GrizzlyBears();
        FellHorseman card = new FellHorseman();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetNonCreatureCard() {
        Plains target = new Plains();
        FellHorseman card = new FellHorseman();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        GrizzlyBears target = new GrizzlyBears();
        FellHorseman card = new FellHorseman();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fell Horseman");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void whenItDiesItIsPutOnTheBottomOfItsOwnersLibrary() {
        FellHorseman card = new FellHorseman();
        harness.addToBattlefield(player1, card);
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new Dreadbore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent horseman = findPermanent(player1, "Fell Horseman");
        harness.castSorcery(player1, 0, horseman.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard, card);
    }
}

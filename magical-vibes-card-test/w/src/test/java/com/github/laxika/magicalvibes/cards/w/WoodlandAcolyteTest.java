package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MendTheWilds;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WoodlandAcolyte.class, MendTheWilds.class, Forest.class, GrizzlyBears.class, Shock.class})
class WoodlandAcolyteTest extends BaseCardTest {

    @Test
    void entersTheBattlefieldAndDrawsACard() {
        Forest draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(new WoodlandAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Woodland Acolyte");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void adventurePutsTargetPermanentOnTopOfLibraryAndExilesCard() {
        WoodlandAcolyte card = new WoodlandAcolyte();
        Card target = new GrizzlyBears();
        Card oldTop = new Forest();
        harness.setHand(player1, List.of(card));
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(oldTop));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target, oldTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetNonPermanentCard() {
        WoodlandAcolyte card = new WoodlandAcolyte();
        Card target = new Shock();
        harness.setHand(player1, List.of(card));
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adventureCannotTargetAnOpponentsGraveyard() {
        Forest target = new Forest();
        harness.setHand(player1, List.of(new WoodlandAcolyte()));
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adventureReturnsLandAndCreatureDrawsItWhenCastFromExile() {
        WoodlandAcolyte card = new WoodlandAcolyte();
        Forest target = new Forest();
        harness.setHand(player1, List.of(card));
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Woodland Acolyte");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Woodland Acolyte");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void adventureWithRemovedTargetGoesToGraveyardInsteadOfExile() {
        WoodlandAcolyte card = new WoodlandAcolyte();
        Forest target = new Forest();
        Forest oldTop = new Forest();
        harness.setHand(player1, List.of(card));
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(oldTop));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAdventure(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(oldTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }
}

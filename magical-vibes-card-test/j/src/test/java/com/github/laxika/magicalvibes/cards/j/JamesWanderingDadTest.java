package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.FollowHim;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JamesWanderingDad.class, FollowHim.class})
class JamesWanderingDadTest extends BaseCardTest {

    @Test
    void adventureInvestigatesXTimesAndExilesTheCard() {
        JamesWanderingDad card = new JamesWanderingDad();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, 2, Map.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(gd.findExiledCard(card.getId()).card()).isSameAs(card);
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        JamesWanderingDad card = new JamesWanderingDad();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAdventure(player1, 0, 0, Map.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "James, Wandering Dad");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void manaAbilityAddsTwoColorlessManaOnlyForAbilities() {
        Permanent james = addCreatureReady(player1, new JamesWanderingDad());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(james.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.COLORLESS))
                .isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void restrictedManaPaysForClueActivation() {
        JamesWanderingDad adventure = new JamesWanderingDad();
        JamesWanderingDad drawnCard = new JamesWanderingDad();
        harness.setHand(player1, List.of(adventure));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, 1, Map.of());
        harness.passBothPriorities();
        addCreatureReady(player1, new JamesWanderingDad());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.COLORLESS))
                .isZero();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void restrictedManaCannotPayForCreatureSpell() {
        addCreatureReady(player1, new JamesWanderingDad());
        JamesWanderingDad spell = new JamesWanderingDad();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.COLORLESS))
                .isEqualTo(2);
    }
}

package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DrownyardBehemoth;
import com.github.laxika.magicalvibes.cards.c.CatharsShield;
import com.github.laxika.magicalvibes.cards.w.WoodlandPatrol;
import com.github.laxika.magicalvibes.cards.u.UlvenwaldObserver;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmrakulsEvangel.class, WoodlandPatrol.class, DrownyardBehemoth.class, CatharsShield.class, UlvenwaldObserver.class})
class EmrakulsEvangelTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices the Evangel and chosen non-Eldrazi creatures to create one token each")
    void sacrificesChosenCreaturesAndCreatesTokens() {
        addCreatureReady(player1, new EmrakulsEvangel());
        Permanent bear = addCreatureReady(player1, new WoodlandPatrol());
        Permanent otherBear = addCreatureReady(player1, new WoodlandPatrol());
        Permanent eldrazi = addCreatureReady(player1, new DrownyardBehemoth());
        harness.addToBattlefield(player1, new CatharsShield());
        harness.addToBattlefield(player2, new WoodlandPatrol());

        harness.activateAbility(player1, 0, 1, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ActivatedAbilityCostChoice.class);
        assertThat(choice.validIds()).containsExactly(bear.getId(), otherBear.getId());

        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Horror")).hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(token.getCard().getColor()).isNull();
                    assertThat(token.getCard().getSubtypes())
                            .containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.HORROR);
                    assertThat(token.getEffectivePower()).isEqualTo(3);
                    assertThat(token.getEffectiveToughness()).isEqualTo(2);
                });
        harness.assertInGraveyard(player1, "Emrakul's Evangel");
        harness.assertInGraveyard(player1, "Woodland Patrol");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherBear, eldrazi);
    }

    @Test
    @DisplayName("Creates one token when no other creature is sacrificed")
    void createsTokenForTheEvangelAlone() {
        addCreatureReady(player1, new EmrakulsEvangel());

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Eldrazi Horror")).hasSize(1);
        harness.assertInGraveyard(player1, "Emrakul's Evangel");
    }

    @Test
    void sacrificesMultipleCreaturesIncludingTappedCreatures() {
        addCreatureReady(player1, new EmrakulsEvangel());
        Permanent first = addCreatureReady(player1, new WoodlandPatrol());
        first.tap();
        harness.addToBattlefield(player1, new WoodlandPatrol());

        harness.activateAbility(player1, 0, 2, null);

        harness.assertNotOnBattlefield(player1, "Emrakul's Evangel");
        harness.assertNotOnBattlefield(player1, "Woodland Patrol");
        assertThat(findPermanents(player1, "Eldrazi Horror")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Horror")).hasSize(3);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new EmrakulsEvangel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Emrakul's Evangel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent evangel = addCreatureReady(player1, new EmrakulsEvangel());
        evangel.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Emrakul's Evangel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCountTheSourceOrEldraziOrOpponentsCreaturesAsAdditionalSacrifices() {
        Permanent evangel = addCreatureReady(player1, new EmrakulsEvangel());
        addCreatureReady(player1, new DrownyardBehemoth());
        addCreatureReady(player2, new WoodlandPatrol());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Emrakul's Evangel");
        assertThat(evangel.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificedObserversSeeEachOtherDieSimultaneously() {
        addCreatureReady(player1, new EmrakulsEvangel());
        addCreatureReady(player1, new UlvenwaldObserver());
        addCreatureReady(player1, new UlvenwaldObserver());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new WoodlandPatrol(), new WoodlandPatrol(),
                new WoodlandPatrol(), new WoodlandPatrol(), new WoodlandPatrol()));

        harness.activateAbility(player1, 0, 2, null);
        for (int i = 0; i < 5; i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(findPermanents(player1, "Eldrazi Horror")).hasSize(3);
    }
}

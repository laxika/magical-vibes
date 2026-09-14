package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.cards.p.PhyrexianScuta;
import com.github.laxika.magicalvibes.cards.s.SeaSnidd;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cavern Harpy")
@CardUsed({CavernHarpy.class, SeaSnidd.class, PhyrexianScuta.class, AlphaKavu.class, ManaCylix.class})
class CavernHarpyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts to return a blue or black creature you control")
    void etbPromptsForBlueOrBlackCreature() {
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new SeaSnidd()).getId();
        UUID blackId = harness.addToBattlefieldAndReturn(player1, new PhyrexianScuta()).getId();
        harness.addToBattlefield(player1, new AlphaKavu());
        harness.addToBattlefield(player1, new ManaCylix());
        harness.addToBattlefield(player2, new SeaSnidd());
        castAndResolveSpell();

        UUID harpyId = harness.getPermanentId(player1, "Cavern Harpy");
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(blueId, blackId, harpyId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("Choosing a matching creature returns it to its owner's hand")
    void chosenMatchingCreatureReturnsToHand() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player1, new PhyrexianScuta()).getId();
        castAndResolveSpell();

        harness.handlePermanentChosen(player1, creatureId);

        harness.assertInHand(player1, "Phyrexian Scuta");
        harness.assertOnBattlefield(player1, "Cavern Harpy");
    }

    @Test
    @DisplayName("Choosing Cavern Harpy itself returns it to its owner's hand")
    void chosenSelfReturnsToHand() {
        castAndResolveSpell();

        UUID harpyId = harness.getPermanentId(player1, "Cavern Harpy");
        harness.handlePermanentChosen(player1, harpyId);

        harness.assertInHand(player1, "Cavern Harpy");
        harness.assertNotOnBattlefield(player1, "Cavern Harpy");
    }

    @Test
    @DisplayName("The ETB ability returns a controlled creature to its owner's hand")
    void etbReturnsControlledCreatureToOwnersHand() {
        Permanent stolenCreature = addStolenPermanent(new PhyrexianScuta());

        castAndResolveSpell();
        harness.handlePermanentChosen(player1, stolenCreature.getId());

        harness.assertInHand(player2, "Phyrexian Scuta");
        harness.assertNotInHand(player1, "Phyrexian Scuta");
        harness.assertOnBattlefield(player1, "Cavern Harpy");
    }

    @Test
    @DisplayName("Paying 1 life returns Cavern Harpy to its owner's hand")
    void payLifeReturnsSelfToHand() {
        harness.addToBattlefield(player1, new CavernHarpy());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        harness.assertInHand(player1, "Cavern Harpy");
        harness.assertNotOnBattlefield(player1, "Cavern Harpy");
    }

    @Test
    @DisplayName("Cannot pay 1 life from 0 life")
    void cannotPayLifeFromZero() {
        harness.addToBattlefield(player1, new CavernHarpy());
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Paying 1 life returns a controlled Cavern Harpy to its owner's hand")
    void payLifeReturnsSelfToOwnersHand() {
        Permanent harpy = addStolenPermanent(new CavernHarpy());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        harness.assertInHand(player2, "Cavern Harpy");
        harness.assertNotInHand(player1, "Cavern Harpy");
        harness.assertNotOnBattlefield(player1, "Cavern Harpy");
    }

    private void castAndResolveSpell() {
        harness.castFromHand(player1, new CavernHarpy(), "{U}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addStolenPermanent(Card card) {
        card.setOwnerId(player2.getId());
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, card);
        gd.stolenCreatures.put(permanent.getId(), player2.getId());
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player1.getId(), permanent,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));
        return permanent;
    }
}

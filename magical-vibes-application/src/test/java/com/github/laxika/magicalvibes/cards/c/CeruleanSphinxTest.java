package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CeruleanSphinx.class)
class CeruleanSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {U} shuffles Cerulean Sphinx into its owner's library")
    void activateShufflesIntoOwnersLibrary() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new CeruleanSphinx());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sphinx);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card instanceof CeruleanSphinx);
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card instanceof CeruleanSphinx);
    }

    @Test
    @DisplayName("Shuffles into its owner's library when controlled by another player")
    void shufflesIntoOwnersLibraryWhenControlledByAnotherPlayer() {
        CeruleanSphinx card = new CeruleanSphinx();
        card.setOwnerId(player1.getId());
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, card);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player2.getId(), sphinx,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(sphinx);
        assertThat(gd.playerDecks.get(player1.getId())).contains(card);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Ability cannot be activated without paying {U}")
    void requiresMana() {
        harness.addToBattlefield(player1, new CeruleanSphinx());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Sphinx can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new CeruleanSphinx());
        sphinx.tap();
        sphinx.setSummoningSick(true);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sphinx);
        assertThat(gd.playerDecks.get(player1.getId())).contains(sphinx.getCard());
    }

    @Test
    @DisplayName("Repeated activations do not duplicate the Sphinx after it leaves the battlefield")
    void repeatedActivationsDoNotDuplicateCard() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new CeruleanSphinx());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sphinx);
        assertThat(gd.playerDecks.get(player1.getId())).containsOnlyOnce(sphinx.getCard());

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsOnlyOnce(sphinx.getCard());
    }
}

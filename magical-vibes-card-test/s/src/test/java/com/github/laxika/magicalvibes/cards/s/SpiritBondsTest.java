package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiritBonds.class, RuneclawBear.class, Opalescence.class})
class SpiritBondsTest extends BaseCardTest {

    @Test
    void payingWhiteCreatesSpiritTokenForNontokenCreature() {
        harness.addToBattlefield(player1, new SpiritBonds());
        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Spirit"))
                .singleElement()
                .satisfies(spirit -> {
                    assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
                    assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
                });
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void activatedAbilitySacrificesSpiritAndGrantsIndestructible() {
        harness.addToBattlefield(player1, new SpiritBonds());
        Permanent spirit = createSpiritToken();
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spirit);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    void activatedAbilityCannotTargetSpirit() {
        harness.addToBattlefield(player1, new SpiritBonds());
        Permanent spirit = createSpiritToken();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spirit.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void decliningPaymentCreatesNoToken() {
        harness.addToBattlefield(player1, new SpiritBonds());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.enterBattlefieldAndReturn(player1, new RuneclawBear());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Spirit")).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentsCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SpiritBonds());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.enterBattlefieldAndReturn(player2, new RuneclawBear());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    void abilityCanProtectOpponentsNonSpiritCreature() {
        harness.addToBattlefield(player1, new SpiritBonds());
        Permanent spirit = createSpiritToken();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spirit);
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        harness.passBothPriorities();

        assertThat(target.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    void abilityCannotBeActivatedWithoutSpiritToSacrifice() {
        harness.addToBattlefield(player1, new SpiritBonds());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void animatedSpiritBondsTriggersForItsOwnEntry() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.enterBattlefieldAndReturn(player1, new SpiritBonds());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void indestructibleExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new SpiritBonds());
        createSpiritToken();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    @Test
    void abilityCannotTargetNoncreaturePermanent() {
        Permanent bonds = harness.addToBattlefieldAndReturn(player1, new SpiritBonds());
        createSpiritToken();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bonds.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    void opponentsSpiritCannotPaySacrificeCost() {
        harness.addToBattlefield(player1, new SpiritBonds());
        Permanent spirit = createSpiritToken();
        gd.playerBattlefields.get(player1.getId()).remove(spirit);
        gd.playerBattlefields.get(player2.getId()).add(spirit);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(spirit);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tokenCreatureEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new SpiritBonds());
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent spirit = createSpiritToken();
        gd.playerBattlefields.get(player1.getId()).remove(spirit);

        harness.enterBattlefieldAndReturn(player1, spirit.getCard());

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent createSpiritToken() {
        Card card = new Card();
        card.setName("Spirit");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.SPIRIT));
        card.setToken(true);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}

package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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

@CardUsed({DonAndresTheRenegade.class, RuneclawBear.class, Divination.class})
class DonAndresTheRenegadeTest extends BaseCardTest {

    @Test
    @DisplayName("Buffs controlled creatures their controller does not own and makes them Pirates")
    void buffsControlledButNotOwnedCreatures() {
        harness.addToBattlefield(player1, new DonAndresTheRenegade());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent stolenCreature = addStolenCreature(player1, player2);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, stolenCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stolenCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, stolenCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, stolenCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, stolenCreature, CardSubtype.PIRATE))
                .isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, opponentCreature, CardSubtype.PIRATE))
                .isFalse();
    }

    @Test
    @DisplayName("Creates two tapped Treasures for a noncreature spell the controller does not own")
    void createsTappedTreasuresForNonOwnedNoncreatureSpell() {
        harness.addToBattlefield(player1, new DonAndresTheRenegade());
        Card spell = new Divination();
        spell.setOwnerId(player2.getId());
        harness.castFromHand(player1, spell, "{2}{U}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2)
                .allMatch(Permanent::isTapped);
    }

    @Test
    void doesNotTriggerForOwnedNoncreatureOrNoncreatureCondition() {
        harness.addToBattlefield(player1, new DonAndresTheRenegade());
        Card ownedSpell = new Divination();
        ownedSpell.setOwnerId(player1.getId());
        harness.castFromHand(player1, ownedSpell, "{2}{U}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        Card ownedCreature = new RuneclawBear();
        ownedCreature.setOwnerId(player2.getId());
        harness.castFromHand(player1, ownedCreature, "{1}{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void bonusesAndGrantedSubtypeDisappearWhenDonAndresLeaves() {
        Permanent donAndres = harness.addToBattlefieldAndReturn(player1, new DonAndresTheRenegade());
        Permanent stolenCreature = addStolenCreature(player1, player2);

        assertThat(gqs.getEffectivePower(gd, stolenCreature)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, stolenCreature, CardSubtype.PIRATE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, stolenCreature, CardSubtype.BEAR)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, donAndres));

        assertThat(gqs.getEffectivePower(gd, stolenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stolenCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, stolenCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, stolenCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, stolenCreature, CardSubtype.PIRATE)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, stolenCreature, CardSubtype.BEAR)).isTrue();
    }

    @Test
    void stolenDonAndresBuffsItself() {
        Permanent donAndres = addStolenCreature(player1, player2, new DonAndresTheRenegade());

        assertThat(gqs.getEffectivePower(gd, donAndres)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, donAndres)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, donAndres, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, donAndres, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void treasureTriggerResolvesAfterDonAndresLeaves() {
        Permanent donAndres = harness.addToBattlefieldAndReturn(player1, new DonAndresTheRenegade());
        Card spell = new Divination();
        spell.setOwnerId(player2.getId());
        harness.castFromHand(player1, spell, "{2}{U}");

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, donAndres));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2)
                .allMatch(Permanent::isTapped);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void opponentsNonOwnedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new DonAndresTheRenegade());
        Card spell = new Divination();
        spell.setOwnerId(player1.getId());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, "{2}{U}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    private Permanent addStolenCreature(Player controller, Player owner) {
        return addStolenCreature(controller, owner, new RuneclawBear());
    }

    private Permanent addStolenCreature(Player controller, Player owner, Card card) {
        card.setOwnerId(owner.getId());
        Permanent permanent = harness.addToBattlefieldAndReturn(owner, card);
        gd.stolenCreatures.put(permanent.getId(), owner.getId());
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, controller.getId(), permanent,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));
        return permanent;
    }
}

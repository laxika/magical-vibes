package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DonAndresTheRenegade.class, GrizzlyBears.class, Divination.class})
class DonAndresTheRenegadeTest extends BaseCardTest {

    @Test
    @DisplayName("Buffs controlled creatures their controller does not own and makes them Pirates")
    void buffsControlledButNotOwnedCreatures() {
        harness.addToBattlefield(player1, new DonAndresTheRenegade());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent stolenCreature = addStolenCreature(player1, player2);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2)
                .allMatch(Permanent::isTapped);
    }

    @Test
    void doesNotTriggerForOwnedNoncreatureOrNoncreatureCondition() {
        harness.addToBattlefield(player1, new DonAndresTheRenegade());
        Card ownedSpell = new Divination();
        ownedSpell.setOwnerId(player1.getId());
        harness.setHand(player1, List.of(ownedSpell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        Card ownedCreature = new GrizzlyBears();
        ownedCreature.setOwnerId(player2.getId());
        harness.setHand(player1, List.of(ownedCreature));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private Permanent addStolenCreature(Player controller, Player owner) {
        Card card = new GrizzlyBears();
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

package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PriceOfBeauty;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConceitedWitch.class, PriceOfBeauty.class, GrizzlyBears.class})
class ConceitedWitchTest extends BaseCardTest {

    @Test
    void adventureCreatesWickedRoleAttachedToTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ConceitedWitch card = new ConceitedWitch();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Wicked");
        assertThat(role.getCard().isToken()).isTrue();
        assertThat(role.getCard().isAura()).isTrue();
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCanOnlyTargetCreatureYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConceitedWitch()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ConceitedWitch card = new ConceitedWitch();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Conceited Witch");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }
}

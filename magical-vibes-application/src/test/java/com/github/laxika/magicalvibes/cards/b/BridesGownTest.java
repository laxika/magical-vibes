package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BridesGown.class, GrizzlyBears.class})
class BridesGownTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusTwoPlusZero() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent gown = addGownReady(player1);
        gown.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void groomFineryAttachedToCreatureYouControlAddsToughnessAndFirstStrike() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent gown = addGownReady(player1);
        gown.setAttachedTo(creature.getId());

        Permanent finery = addGroomFinery(player2);
        finery.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void groomFineryAttachedToOpponentCreatureDoesNotEnableBonus() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent gown = addGownReady(player1);
        gown.setAttachedTo(creature.getId());

        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent finery = addGroomFinery(player2);
        finery.setAttachedTo(opponentCreature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void differentlyNamedEquipmentDoesNotEnableBonus() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent gown = addGownReady(player1);
        gown.setAttachedTo(creature.getId());

        Permanent otherEquipment = addEquipment(player1, "Other Equipment");
        otherEquipment.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addGownReady(Player player) {
        Permanent permanent = new Permanent(new BridesGown());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addGroomFinery(Player player) {
        return addEquipment(player, "Groom's Finery");
    }

    private Permanent addEquipment(Player player, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

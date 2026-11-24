package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesOneEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "133")
@CardRegistration(set = "WHO", collectorNumber = "738")
public class GreatIntelligencesPlan extends Card {

    public static final String DISCARD_OPTION = "They discard three cards";
    public static final String CAST_OPTION = "You may cast a spell from your hand without paying its mana cost";

    public GreatIntelligencesPlan() {
        addSpellEffect(new DrawCardEffect(3));
        // The opponent always faces the choice, even when neither option would do anything (ruling).
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addSpellEffect(TargetPlayerChoosesOneEffect.forTargetedPlayerVillainousChoice(List.of(
                        new ChooseOneEffect.ChooseOneOption(DISCARD_OPTION,
                                new DiscardEffect(3, Subjects.targetPlayerDeclaredElsewhere())),
                        new ChooseOneEffect.ChooseOneOption(CAST_OPTION,
                                new MayCastAnySpellFromHandWithoutPayingManaCostEffect()))));
    }
}

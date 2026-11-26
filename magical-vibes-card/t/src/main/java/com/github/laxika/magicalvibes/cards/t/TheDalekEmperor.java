package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesOneEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "120")
@CardRegistration(set = "WHO", collectorNumber = "406")
@CardRegistration(set = "WHO", collectorNumber = "725")
@CardRegistration(set = "WHO", collectorNumber = "997")
public class TheDalekEmperor extends Card {

    public TheDalekEmperor() {
        // Affinity for Daleks.
        addStaticEffect(new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.DALEK), CountScope.CONTROLLER)));

        // Other Daleks you control have haste.
        addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.otherCreaturesYouControl(),
                new PermanentHasSubtypePredicate(CardSubtype.DALEK)));

        // Each opponent faces a villainous choice: that player sacrifices a creature, or you create a Dalek. Either
        // option may be chosen, even an impossible one (CR 701.55b).
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                TargetPlayerChoosesOneEffect.villainousChoiceFacedBy(Subjects.eachOpponent(), List.of(
                        new ChooseOneEffect.ChooseOneOption("That player sacrifices a creature",
                                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                                        Subjects.targetPlayerDeclaredElsewhere())),
                        new ChooseOneEffect.ChooseOneOption("You create a Dalek", new CreateTokenEffect(
                                "Dalek", 3, 3, CardColor.BLACK,
                                List.of(CardSubtype.DALEK), Set.of(Keyword.MENACE),
                                Set.of(CardType.ARTIFACT))))));
    }
}

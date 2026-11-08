package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DoubleXValueOfTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;

@CardRegistration(set = "SLD", collectorNumber = "1331")
@CardRegistration(set = "MH1", collectorNumber = "189")
@CardRegistration(set = "SOC", collectorNumber = "126")
public class UnboundFlourishing extends Card {

    public UnboundFlourishing() {
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardHasXInManaCostPredicate()))),
                new DoubleXValueOfTriggeringSpellEffect());

        addTrigger(Triggers.youCastSpell().withFilter(new CardAllOfPredicate(List.of(
                        new CardHasXInManaCostPredicate(),
                        new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)))))),
                new CopyTriggeringSpellEffect());

        addTrigger(Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.WITH_X_IN_COST),
                new CopyTriggeringSpellOrAbilityEffect());
    }
}

package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SkipRecipient;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "265")
public class DovinArchitectOfLaw extends Card {

    public DovinArchitectOfLaw() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new GainLifeEffect(2), new DrawCardEffect(1)),
                "+1: You gain 2 life and draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new SkipNextUntapEffect(TapUntapScope.TARGET)
                ),
                "−1: Tap target creature. It doesn't untap during its controller's next untap step.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS),
                        new SkipNextEffect(SkipKind.UNTAP_STEP, SkipRecipient.TARGET_PLAYER)
                ),
                "−9: Tap all permanents target opponent controls. That player skips their next untap step.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}

package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.StabwhiskerTheOdious;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "131")
public class NezumiShortfang extends Card {

    public NezumiShortfang() {
        setBackFaceCard(new StabwhiskerTheOdious());

        // {1}{B}, {T}: Target opponent discards a card. Then if that player has no cards in hand,
        // flip this creature.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}{B}",
                List.of(
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, false),
                        new ConditionalEffect(new TargetPlayerHandEmpty(), new TransformSelfEffect())
                ),
                "{1}{B}, {T}: Target opponent discards a card. Then if that player has no cards in "
                        + "hand, flip this creature.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "StabwhiskerTheOdious";
    }
}

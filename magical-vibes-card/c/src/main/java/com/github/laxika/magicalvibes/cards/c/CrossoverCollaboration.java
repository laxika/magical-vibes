package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TeamworkCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TeamworkCost;

@CardRegistration(set = "MSC", collectorNumber = "684")
public class CrossoverCollaboration extends Card {

    public CrossoverCollaboration() {
        addCastingCost(new TeamworkCost(2));
        addSpellEffect(new ExileTopCardMayPlayThisTurnEffect(2, false).until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN));
        addSpellEffect(new ConditionalEffect(
                new TeamworkCostPaid(), CreateTokenEffect.ofTreasureToken(1)));
    }
}

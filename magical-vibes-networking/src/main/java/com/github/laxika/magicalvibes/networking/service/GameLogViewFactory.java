package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameLogSegment;
import com.github.laxika.magicalvibes.networking.model.GameLogEntryView;
import com.github.laxika.magicalvibes.networking.model.GameLogSegmentView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GameLogViewFactory {

    private final CardViewFactory cardViewFactory;

    public GameLogEntryView create(GameLogEntry entry) {
        List<GameLogSegmentView> segments = entry.segments().stream()
                .map(this::createSegment)
                .toList();
        return new GameLogEntryView(segments);
    }

    public List<GameLogEntryView> createAll(List<GameLogEntry> entries) {
        return entries.stream().map(this::create).toList();
    }

    private GameLogSegmentView createSegment(GameLogSegment segment) {
        return switch (segment) {
            case GameLogSegment.Text text -> GameLogSegmentView.text(text.value());
            case GameLogSegment.CardSegment card -> GameLogSegmentView.card(cardViewFactory.create(card.card()));
        };
    }
}
